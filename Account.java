// Java version of Account class with password support for web authentication
// Demonstrates Inheritance and Interface implementation
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.FileWriter;
import java.io.IOException;

public class Account extends BankEntity implements Authenticatable {
    protected double balance;
    protected String password;
    protected List<String> transactionHistory;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Account(String accountNumber, String accountHolderName, double balance, String password) {
        super(accountNumber, accountHolderName);
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
        return getId();
    }

    public String getAccountHolderName() {
        return getName();
    }

    public double getBalance() {
        return balance;
    }

    public String getPassword() {
        return password;
    }

    @Override
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
        System.out.println("\n--- Transaction History for Account " + getAccountNumber() + " ---");
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
            writer.write("Account Number: " + getAccountNumber() + "\n");
            writer.write("Account Type: " + getEntityType() + "\n");
            writer.write("Account Holder: " + getAccountHolderName() + "\n");
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
@Override
    public boolean authenticate(String password) {
        return this.password.equals(password);
    }

    @Override
    public String getEntityType() {
        return "Account";
    }

    @Override
    public String displayInfo() {
        return String.format("Account{accountNumber='%s', accountHolderName='%s', accountType='%s', balance=%.2f}", 
            getAccountNumber(), getAccountHolderName(), getEntityType(), balance);
    }

    @Override
    public String toString() {
        return displayInfo();
    }
}
