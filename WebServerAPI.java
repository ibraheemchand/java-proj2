import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.*;

/**
 * Enhanced WebServer with API endpoints and static file serving
 * Provides both HTML UI and JSON-based REST API for banking operations
 */
public class WebServerAPI {
    private static final String WEB_ROOT = "web";
    private static Bank sharedBank;
    private static Admin admin;
    private static Map<String, String> sessions; // token -> accountNumber

    public static void main(String[] args) {
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port. Using default 8080.");
            }
        }

        // Initialize shared bank and admin
        sharedBank = new Bank();
        admin = new Admin("admin", "admin123");
        sessions = new HashMap<>();

        HttpServer server = null;
        int maxTries = 10;
        int tries = 0;

        while (tries < maxTries) {
            try {
                server = HttpServer.create(new InetSocketAddress(port), 0);
                break;
            } catch (Exception e) {
                System.out.println("Port " + port + " is in use. Trying " + (port + 1) + "...");
                port++;
                tries++;
            }
        }

        if (server == null) {
            System.out.println("Could not bind to any port.");
            return;
        }

        // Register handlers
        server.createContext("/", new StaticFileHandler());
        server.createContext("/login/admin", new AdminLoginHandler());
        server.createContext("/login/user", new UserLoginHandler());
        server.createContext("/logout", new LogoutHandler());
        server.createContext("/accounts", new AccountsHandler());
        server.createContext("/create-account", new CreateAccountHandler());
        server.createContext("/transfer", new TransferHandler());
        server.createContext("/user-transfer", new UserTransferHandler());
        server.createContext("/change-password", new ChangePasswordHandler());
        server.createContext("/my-account", new MyAccountHandler());

        server.setExecutor(null);
        System.out.println("✓ Bank API Server running at http://localhost:" + port);
        System.out.println("✓ Web UI: http://localhost:" + port);
        System.out.println("✓ Demo accounts: Account 1 (alice123) | Account 2 (bob123)");
        server.start();
    }

    // ─────────────────────────────────────────────────────────────────
    // HANDLERS
    // ─────────────────────────────────────────────────────────────────

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            File file = new File(WEB_ROOT, path);
            if (file.exists() && !file.isDirectory()) {
                String mime = getMimeType(file.getName());
                exchange.getResponseHeaders().set("Content-Type", mime);
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                byte[] bytes = Files.readAllBytes(file.toPath());
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } else {
                String resp = "404 Not Found";
                exchange.sendResponseHeaders(404, resp.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(resp.getBytes());
                }
            }
        }

        private String getMimeType(String name) {
            if (name.endsWith(".html")) return "text/html";
            if (name.endsWith(".css")) return "text/css";
            if (name.endsWith(".js")) return "application/javascript";
            if (name.endsWith(".json")) return "application/json";
            if (name.endsWith(".png")) return "image/png";
            if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
            return "application/octet-stream";
        }
    }

    static class AdminLoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }
            try {
                String body = readBody(exchange);
                String password = extractJsonValue(body, "password");
                if (password == null) {
                    sendJson(exchange, 400, "{\"status\":\"err\",\"message\":\"Missing password\"}");
                    return;
                }
                if (admin.authenticate(password)) {
                    String token = "admin_" + UUID.randomUUID();
                    sessions.put(token, "ADMIN");
                    sendJson(exchange, 200, "{\"status\":\"ok\",\"message\":\"Admin login successful\",\"token\":\"" + token + "\"}");
                } else {
                    sendJson(exchange, 401, "{\"status\":\"err\",\"message\":\"Invalid password\"}");
                }
            } catch (Exception e) {
                sendJson(exchange, 500, "{\"status\":\"err\",\"message\":\"Server error\"}");
            }
        }
    }

    static class UserLoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }
            try {
                String body = readBody(exchange);
                String accountNumber = extractJsonValue(body, "accountNumber");
                String password = extractJsonValue(body, "password");
                if (accountNumber == null || password == null) {
                    sendJson(exchange, 400, "{\"status\":\"err\",\"message\":\"Missing credentials\"}");
                    return;
                }
                Account acc = sharedBank.findAccount(accountNumber);
                if (acc != null && acc.authenticate(password)) {
                    String token = "user_" + UUID.randomUUID();
                    sessions.put(token, accountNumber);
                    String resp = String.format(
                        "{\"status\":\"ok\",\"message\":\"Login successful\",\"token\":\"%s\",\"account\":{\"accountNumber\":\"%s\",\"holderName\":\"%s\",\"balance\":%.2f}}",
                        token, acc.getAccountNumber(), acc.getAccountHolderName(), acc.getBalance()
                    );
                    sendJson(exchange, 200, resp);
                } else {
                    sendJson(exchange, 401, "{\"status\":\"err\",\"message\":\"Invalid credentials\"}");
                }
            } catch (Exception e) {
                sendJson(exchange, 500, "{\"status\":\"err\",\"message\":\"Server error\"}");
            }
        }
    }

    static class LogoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String token = getAuthToken(exchange);
            if (token != null) {
                sessions.remove(token);
            }
            sendJson(exchange, 200, "{\"status\":\"ok\",\"message\":\"Logged out\"}");
        }
    }

    static class AccountsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!isAuthenticated(exchange)) {
                sendJson(exchange, 401, "{\"status\":\"err\",\"message\":\"Unauthorized\"}");
                return;
            }
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < sharedBank.getAccounts().size(); i++) {
                Account a = sharedBank.getAccounts().get(i);
                if (i > 0) json.append(",");
                json.append(String.format(
                    "{\"accountNumber\":\"%s\",\"holderName\":\"%s\",\"balance\":%.2f}",
                    a.getAccountNumber(), a.getAccountHolderName(), a.getBalance()
                ));
            }
            json.append("]");
            sendJson(exchange, 200, json.toString());
        }
    }

    static class CreateAccountHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!isAdminAuthenticated(exchange)) {
                sendJson(exchange, 401, "{\"status\":\"err\",\"message\":\"Unauthorized\"}");
                return;
            }
            try {
                String body = readBody(exchange);
                String accNum = extractJsonValue(body, "accountNumber");
                String name = extractJsonValue(body, "holderName");
                String balStr = extractJsonValue(body, "initialBalance");
                String password = extractJsonValue(body, "password");
                if (accNum == null || name == null || balStr == null || password == null) {
                    sendJson(exchange, 400, "{\"status\":\"err\",\"message\":\"Missing fields\"}");
                    return;
                }
                if (sharedBank.findAccount(accNum) != null) {
                    sendJson(exchange, 400, "{\"status\":\"err\",\"message\":\"Account already exists\"}");
                    return;
                }
                double balance = Double.parseDouble(balStr);
                Account newAcc = new Account(accNum, name, balance, password);
                sharedBank.addAccount(newAcc);
                sendJson(exchange, 200, "{\"status\":\"ok\",\"message\":\"Account created successfully\"}");
            } catch (Exception e) {
                sendJson(exchange, 500, "{\"status\":\"err\",\"message\":\"Error creating account: " + e.getMessage() + "\"}");
            }
        }
    }

    static class TransferHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!isAdminAuthenticated(exchange)) {
                sendJson(exchange, 401, "{\"status\":\"err\",\"message\":\"Unauthorized\"}");
                return;
            }
            try {
                String body = readBody(exchange);
                String from = extractJsonValue(body, "fromAccount");
                String to = extractJsonValue(body, "toAccount");
                String amtStr = extractJsonValue(body, "amount");
                if (from == null || to == null || amtStr == null) {
                    sendJson(exchange, 400, "{\"status\":\"err\",\"message\":\"Missing fields\"}");
                    return;
                }
                if (from.equals(to)) {
                    sendJson(exchange, 400, "{\"status\":\"err\",\"message\":\"Cannot transfer to same account\"}");
                    return;
                }
                Account fromAcc = sharedBank.findAccount(from);
                Account toAcc = sharedBank.findAccount(to);
                if (fromAcc == null || toAcc == null) {
                    sendJson(exchange, 400, "{\"status\":\"err\",\"message\":\"Account not found\"}");
                    return;
                }
                double amount = Double.parseDouble(amtStr);
                if (!fromAcc.withdraw(amount)) {
                    sendJson(exchange, 400, "{\"status\":\"err\",\"message\":\"Insufficient funds\"}");
                    return;
                }
                toAcc.deposit(amount);
                sendJson(exchange, 200, "{\"status\":\"ok\",\"message\":\"Transfer successful\"}");
            } catch (Exception e) {
                sendJson(exchange, 500, "{\"status\":\"err\",\"message\":\"Error: " + e.getMessage() + "\"}");
            }
        }
    }

    static class UserTransferHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String token = getAuthToken(exchange);
            if (token == null || !sessions.containsKey(token) || sessions.get(token).equals("ADMIN")) {
                sendJson(exchange, 401, "{\"status\":\"err\",\"message\":\"Unauthorized\"}");
                return;
            }
            try {
                String fromAccount = sessions.get(token);
                String body = readBody(exchange);
                String toAccount = extractJsonValue(body, "toAccount");
                String amtStr = extractJsonValue(body, "amount");
                if (toAccount == null || amtStr == null) {
                    sendJson(exchange, 400, "{\"status\":\"err\",\"message\":\"Missing fields\"}");
                    return;
                }
                Account from = sharedBank.findAccount(fromAccount);
                Account to = sharedBank.findAccount(toAccount);
                if (from == null || to == null) {
                    sendJson(exchange, 400, "{\"status\":\"err\",\"message\":\"Account not found\"}");
                    return;
                }
                double amount = Double.parseDouble(amtStr);
                if (!from.withdraw(amount)) {
                    sendJson(exchange, 400, "{\"status\":\"err\",\"message\":\"Insufficient funds\"}");
                    return;
                }
                to.deposit(amount);
                sendJson(exchange, 200, "{\"status\":\"ok\",\"message\":\"Transfer successful\"}");
            } catch (Exception e) {
                sendJson(exchange, 500, "{\"status\":\"err\",\"message\":\"Error: " + e.getMessage() + "\"}");
            }
        }
    }

    static class ChangePasswordHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!isAdminAuthenticated(exchange)) {
                sendJson(exchange, 401, "{\"status\":\"err\",\"message\":\"Unauthorized\"}");
                return;
            }
            try {
                String body = readBody(exchange);
                String accNum = extractJsonValue(body, "accountNumber");
                String newPassword = extractJsonValue(body, "newPassword");
                if (accNum == null || newPassword == null) {
                    sendJson(exchange, 400, "{\"status\":\"err\",\"message\":\"Missing fields\"}");
                    return;
                }
                Account acc = sharedBank.findAccount(accNum);
                if (acc == null) {
                    sendJson(exchange, 400, "{\"status\":\"err\",\"message\":\"Account not found\"}");
                    return;
                }
                acc.setPassword(newPassword);
                sendJson(exchange, 200, "{\"status\":\"ok\",\"message\":\"Password updated\"}");
            } catch (Exception e) {
                sendJson(exchange, 500, "{\"status\":\"err\",\"message\":\"Error: " + e.getMessage() + "\"}");
            }
        }
    }

    static class MyAccountHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String token = getAuthToken(exchange);
            if (token == null || !sessions.containsKey(token) || sessions.get(token).equals("ADMIN")) {
                sendJson(exchange, 401, "{\"status\":\"err\",\"message\":\"Unauthorized\"}");
                return;
            }
            try {
                String accountNumber = sessions.get(token);
                Account acc = sharedBank.findAccount(accountNumber);
                if (acc == null) {
                    sendJson(exchange, 404, "{\"status\":\"err\",\"message\":\"Account not found\"}");
                    return;
                }
                String resp = String.format(
                    "{\"accountNumber\":\"%s\",\"holderName\":\"%s\",\"balance\":%.2f}",
                    acc.getAccountNumber(), acc.getAccountHolderName(), acc.getBalance()
                );
                sendJson(exchange, 200, resp);
            } catch (Exception e) {
                sendJson(exchange, 500, "{\"status\":\"err\",\"message\":\"Error: " + e.getMessage() + "\"}");
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // UTILITY METHODS
    // ─────────────────────────────────────────────────────────────────

    private static boolean isAuthenticated(HttpExchange exchange) {
        String token = getAuthToken(exchange);
        return token != null && sessions.containsKey(token);
    }

    private static boolean isAdminAuthenticated(HttpExchange exchange) {
        String token = getAuthToken(exchange);
        return token != null && sessions.containsKey(token) && sessions.get(token).equals("ADMIN");
    }

    private static String getAuthToken(HttpExchange exchange) {
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        return null;
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (InputStream is = exchange.getRequestBody()) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = is.read(buffer)) != -1) {
                sb.append(new String(buffer, 0, read));
            }
        }
        return sb.toString();
    }

    private static String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":\"";
        int start = json.indexOf(searchKey);
        if (start == -1) {
            // Try without quotes for numbers
            searchKey = "\"" + key + "\":";
            start = json.indexOf(searchKey);
            if (start == -1) return null;
            start += searchKey.length();
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
            return json.substring(start, end).trim();
        }
        start += searchKey.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    private static void sendJson(HttpExchange exchange, int code, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        byte[] bytes = json.getBytes();
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendError(HttpExchange exchange, int code, String message) throws IOException {
        sendJson(exchange, code, "{\"status\":\"err\",\"message\":\"" + message + "\"}");
    }
}
