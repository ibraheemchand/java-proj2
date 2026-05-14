import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.BindException;
import java.nio.file.Files;

public class WebServer {
    private static final String WEB_ROOT = "web";

    /**
     * Start the HTTP server on the given port. If the port is in use, try the next available port.
     * Optionally, allow the user to specify a port as a command-line argument.
     */
    public static void main(String[] args) {
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port argument. Using default port 8080.");
            }
        }
        HttpServer server = null;
        int maxTries = 10;
        int tries = 0;
        while (tries < maxTries) {
            try {
                server = HttpServer.create(new InetSocketAddress(port), 0);
                break;
            } catch (BindException e) {
                System.out.println("Port " + port + " is already in use. Trying next port...");
                port++;
                tries++;
            } catch (IOException e) {
                System.out.println("Failed to start server: " + e.getMessage());
                return;
            }
        }
        if (server == null) {
            System.out.println("Could not bind to any port in range. Exiting.");
            return;
        }
        server.createContext("/", new StaticFileHandler());
        server.setExecutor(null);
        System.out.println("Server started at http://localhost:" + port);
        server.start();
    }

    /**
     * Handler for serving static files from the web directory.
     */
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            File file = new File(WEB_ROOT, path);
            if (file.exists() && !file.isDirectory()) {
                String mime = getMimeType(file.getName());
                exchange.getResponseHeaders().set("Content-Type", mime);
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
}
