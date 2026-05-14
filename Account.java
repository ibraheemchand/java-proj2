// Java version of Account class with password support for web authentication
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.FileWriter;
import java.io.IOException;

public class Account {
    private String accountNumber;
    private String accountHolderName;
    private double balance;
    private String password;
    private List<String> transactionHistory;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Account(String accountNumber, String accountHolderName, double balance, String password) {
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.balance = balance;
        this.password = password;
        this.transactionHistory = new ArrayList<>();
        // Add account creation entry
        recordTransaction("ACCOUNT CREATED", 0, balance);
    }

    // Constructor for backward compatibility with console app
    public Account(int accountNumber, String accountHolderName, double balance) {
        this(String.valueOf(accountNumber), accountHolderName, balance, "");
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public double getBalance() {
        return balance;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            recordTransaction("DEPOSIT", amount, balance);
        }
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            recordTransaction("WITHDRAWAL", amount, balance);
            return true;
        }
        return false;
    }

    private void recordTransaction(String type, double amount, double balanceAfter) {
        String timestamp = LocalDateTime.now().format(formatter);
        String transaction = String.format("[%s] %s | Amount: %.2f | Balance: %.2f", 
            timestamp, type, amount, balanceAfter);
        transactionHistory.add(transaction);
    }

    public List<String> getTransactionHistory() {
        return new ArrayList<>(transactionHistory);
    }

    public void displayTransactionHistory() {
        if (transactionHistory.isEmpty()) {
            System.out.println("No transactions yet.");
            return;
        }
        System.out.println("\n--- Transaction History for Account " + accountNumber + " ---");
        for (String transaction : transactionHistory) {
            System.out.println(transaction);
        }
        System.out.println("---\n");
    }

    public boolean downloadTransactionHistory(String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("================================================\n");
            writer.write("TRANSACTION HISTORY REPORT\n");
            writer.write("================================================\n");
            writer.write("Account Number: " + accountNumber + "\n");
            writer.write("Account Holder: " + accountHolderName + "\n");
            writer.write("Current Balance: " + balance + "\n");
            writer.write("Report Generated: " + LocalDateTime.now().format(formatter) + "\n");
            writer.write("================================================\n\n");
            
            if (transactionHistory.isEmpty()) {
                writer.write("No transactions found.\n");
            } else {
                for (String transaction : transactionHistory) {
                    writer.write(transaction + "\n");
                }
            }
            
            writer.write("\n================================================\n");
            writer.write("End of Report\n");
            writer.write("================================================\n");
            return true;
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
            return false;
        }
    }

    public boolean authenticate(String password) {
        return this.password.equals(password);
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountNumber='" + accountNumber + '\'' +
                ", accountHolderName='" + accountHolderName + '\'' +
                ", balance=" + balance +
                '}';
    }
}
