// Java version of Bank class
import java.util.ArrayList;
import java.util.List;

public class Bank {
    private List<Account> accounts;

    public Bank() {
        accounts = new ArrayList<>();
        // Initialize with demo accounts - one Savings and one Current
        // Demonstrates Polymorphism: different account types stored in same list
        accounts.add(new SavingsAccount("1", "Alice Johnson", 5000, "alice123", 3.5));
        accounts.add(new CurrentAccount("2", "Bob Smith", 3500, "bob123", 500));
    }

    public void addAccount(Account account) {
        if (findAccount(account.getAccountNumber()) == null) {
            accounts.add(account);
        } else {
            System.out.println("Account already exists.");
        }
    }

    public Account findAccount(String accountNumber) {
        for (Account acc : accounts) {
            if (acc.getAccountNumber().equals(accountNumber)) {
                return acc;
            }
        }
        return null;
    }

    // Legacy method for console app
    public Account findAccount(int accountNumber) {
        return findAccount(String.valueOf(accountNumber));
    }

    public boolean removeAccount(String accountNumber) {
        Account acc = findAccount(accountNumber);
        if (acc != null) {
            accounts.remove(acc);
            return true;
        }
        return false;
    }

    // Legacy method for console app
    public boolean removeAccount(int accountNumber) {
        return removeAccount(String.valueOf(accountNumber));
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    /**
     * Demonstrates Polymorphism - calls getEntityType() on each account
     * at runtime, the correct subclass method is called
     */
    public void printAllAccountTypes() {
        System.out.println("\n========== Account Types Report ==========");
        for (Account account : accounts) {
            // Polymorphic call - actual method called depends on runtime type
            System.out.println("Account " + account.getAccountNumber() + " (" + account.getAccountHolderName() + 
                             "): " + account.getEntityType());
        }
        System.out.println("==========================================\n");
    }

    /**
     * Display all account details using polymorphic displayInfo()
     */
    public void displayAllAccountDetails() {
        System.out.println("\n========== All Account Details ==========");
        for (Account account : accounts) {
            System.out.println(account.displayInfo());
        }
        System.out.println("==========================================\n");
    }
}
