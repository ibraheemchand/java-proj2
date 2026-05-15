/**
 * SavingsAccount class extending Account
 * Demonstrates Inheritance and Polymorphism
 * Adds interest rate calculation functionality
 */
public class SavingsAccount extends Account {
    private double interestRate;

    public SavingsAccount(String accountNumber, String accountHolderName, double balance, 
                          String password, double interestRate) {
        super(accountNumber, accountHolderName, balance, password);
        this.interestRate = interestRate;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    /**
     * Apply monthly interest to the account
     */
    public void applyInterest() {
        double interest = balance * (interestRate / 100) / 12; // Monthly interest
        if (interest > 0) {
            balance += interest;
            recordTransaction("INTEREST APPLIED", interest, balance);
            System.out.println("Interest applied: $" + String.format("%.2f", interest));
        }
    }

    /**
     * Demonstration of interest calculation
     */
    public double calculateProjectedBalance(int months) {
        double projectedBalance = balance;
        for (int i = 0; i < months; i++) {
            projectedBalance += projectedBalance * (interestRate / 100) / 12;
        }
        return projectedBalance;
    }

    @Override
    public String getEntityType() {
        return "Savings Account";
    }

    @Override
    public String displayInfo() {
        return String.format(
            "SavingsAccount{accountNumber='%s', accountHolderName='%s', accountType='%s', balance=%.2f, interestRate=%.2f%%}",
            getAccountNumber(), getAccountHolderName(), getEntityType(), balance, interestRate
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
