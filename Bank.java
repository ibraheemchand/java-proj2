// Java version of Bank class
import java.util.ArrayList;
import java.util.List;

public class Bank {
    private List<Account> accounts;

    public Bank() {
        accounts = new ArrayList<>();
        // Initialize with demo accounts
        accounts.add(new Account("1", "Alice Johnson", 5000, "alice123"));
        accounts.add(new Account("2", "Bob Smith", 3500, "bob123"));
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
}
