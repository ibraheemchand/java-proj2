/**
 * CurrentAccount class extending Account
 * Demonstrates Inheritance and Polymorphism
 * Adds overdraft limit functionality
 */
public class CurrentAccount extends Account {
    private double overdraftLimit;

    public CurrentAccount(String accountNumber, String accountHolderName, double balance,
                          String password, double overdraftLimit) {
        super(accountNumber, accountHolderName, balance, password);
        this.overdraftLimit = overdraftLimit;
    }

    public double getOverdraftLimit() {
        return overdraftLimit;
    }

    public void setOverdraftLimit(double overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }

    /**
     * Get available balance including overdraft
     */
    public double getAvailableBalance() {
        return balance + overdraftLimit;
    }

    /**
     * Override withdraw to allow overdraft
     * Demonstrates Polymorphism - method overriding
     */
    @Override
    public boolean withdraw(double amount) {
        if (amount > 0 && amount <= getAvailableBalance()) {
            balance -= amount;
            recordTransaction("WITHDRAWAL", amount, balance);
            
            if (balance < 0) {
                System.out.println("⚠️  Overdraft used: $" + String.format("%.2f", Math.abs(balance)));
            }
            return true;
        }
        return false;
    }

    /**
     * Check if account is in overdraft
     */
    public boolean isInOverdraft() {
        return balance < 0;
    }

    /**
     * Get overdraft amount being used
     */
    public double getOverdraftUsed() {
        return isInOverdraft() ? Math.abs(balance) : 0;
    }

    @Override
    public String getEntityType() {
        return "Current Account";
    }

    @Override
    public String displayInfo() {
        return String.format(
            "CurrentAccount{accountNumber='%s', accountHolderName='%s', accountType='%s', balance=%.2f, overdraftLimit=%.2f, availableBalance=%.2f}",
            getAccountNumber(), getAccountHolderName(), getEntityType(), balance, overdraftLimit, getAvailableBalance()
        );
    }

    private void recordTransaction(String type, double amount, double balanceAfter) {
        java.time.LocalDateTime timestamp = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String transaction = String.format("[%s] %s | Amount: %.2f | Balance: %.2f",
            timestamp.format(formatter), type, amount, balanceAfter);
        transactionHistory.add(transaction);
    }
}
