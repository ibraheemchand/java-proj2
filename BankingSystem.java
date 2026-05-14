// Java version of main banking system logic
import java.util.Scanner;

public class BankingSystem {
    // Helper method to read an integer with validation
    private static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            try {
                return Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid integer.");
            }
        }
    }

    // Helper method to read a double with validation
    private static double readDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            try {
                return Double.parseDouble(input.trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }

    // Main method with robust input handling and professional UI
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Bank bank = new Bank();
        Admin admin = new Admin("admin", "password");
        boolean running = true;

        System.out.println("\n==============================");
        System.out.println("   Welcome to the Banking System");
        System.out.println("==============================\n");

        while (running) {
            System.out.println("\nMenu:");
            System.out.println("1. Admin Login");
            System.out.println("2. Customer Login");
            System.out.println("3. Add Account");
            System.out.println("4. Deposit");
            System.out.println("5. Withdraw");
            System.out.println("6. View Transaction History");
            System.out.println("7. Download Transaction History");
            System.out.println("8. Remove Account");
            System.out.println("9. Show Accounts");
            System.out.println("10. Exit");
            int choice = readInt(scanner, "Choose an option: ");

            switch (choice) {
                case 1: {
                    System.out.print("Enter admin username: ");
                    String user = scanner.nextLine();
                    System.out.print("Enter admin password: ");
                    String pass = scanner.nextLine();
                    if (admin.authenticate(user, pass)) {
                        System.out.println("Admin login successful.");
                        adminMenu(scanner, bank);
                    } else {
                        System.out.println("Invalid credentials.\n");
                    }
                    break;
                }
                case 2: {
                    System.out.print("Enter account number: ");
                    String accNum = scanner.nextLine();
                    System.out.print("Enter password: ");
                    String pass = scanner.nextLine();
                    Account acc = bank.findAccount(accNum);
                    if (acc != null && acc.authenticate(pass)) {
                        System.out.println("Customer login successful.\n");
                        customerMenu(scanner, acc);
                    } else {
                        System.out.println("Invalid account number or password.\n");
                    }
                    break;
                }
                case 3: {
                    int accNum = readInt(scanner, "Enter account number: ");
                    System.out.print("Enter account holder name: ");
                    String name = scanner.nextLine();
                    double bal = readDouble(scanner, "Enter initial balance: ");
                    System.out.print("Enter password: ");
                    String pwd = scanner.nextLine();
                    bank.addAccount(new Account(String.valueOf(accNum), name, bal, pwd));
                    System.out.println("Account added successfully.\n");
                    break;
                }
                case 4: {
                    int accNum = readInt(scanner, "Enter account number: ");
                    double dep = readDouble(scanner, "Enter deposit amount: ");
                    Account acc = bank.findAccount(String.valueOf(accNum));
                    if (acc != null) {
                        acc.deposit(dep);
                        System.out.println("Deposit successful. New balance: " + acc.getBalance() + "\n");
                    } else {
                        System.out.println("Account not found.\n");
                    }
                    break;
                }
                case 5: {
                    int accNum = readInt(scanner, "Enter account number: ");
                    double wd = readDouble(scanner, "Enter withdrawal amount: ");
                    Account acc = bank.findAccount(String.valueOf(accNum));
                    if (acc != null && acc.withdraw(wd)) {
                        System.out.println("Withdrawal successful. New balance: " + acc.getBalance() + "\n");
                    } else {
                        System.out.println("Insufficient funds or account not found.\n");
                    }
                    break;
                }
                case 6: {
                    int accNum = readInt(scanner, "Enter account number: ");
                    Account acc = bank.findAccount(String.valueOf(accNum));
                    if (acc != null) {
                        acc.displayTransactionHistory();
                    } else {
                        System.out.println("Account not found.\n");
                    }
                    break;
                }
                case 7: {
                    int accNum = readInt(scanner, "Enter account number: ");
                    Account acc = bank.findAccount(String.valueOf(accNum));
                    if (acc != null) {
                        System.out.print("Enter filename to save (e.g., transactions.txt): ");
                        String filename = scanner.nextLine();
                        if (acc.downloadTransactionHistory(filename)) {
                            System.out.println("Transaction history downloaded to: " + filename + "\n");
                        } else {
                            System.out.println("Failed to download transaction history.\n");
                        }
                    } else {
                        System.out.println("Account not found.\n");
                    }
                    break;
                }
                case 8: {
                    int accNum = readInt(scanner, "Enter account number to remove: ");
                    if (bank.removeAccount(String.valueOf(accNum))) {
                        System.out.println("Account removed.\n");
                    } else {
                        System.out.println("Account not found.\n");
                    }
                    break;
                }
                case 9: {
                    System.out.println("\n--- All Accounts ---");
                    for (Account a : bank.getAccounts()) {
                        System.out.println(a);
                    }
                    if (bank.getAccounts().isEmpty()) {
                        System.out.println("No accounts found.");
                    }
                    System.out.println();
                    break;
                }
                case 10:
                    running = false;
                    System.out.println("Thank you for using the Banking System. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please choose a number from 1 to 10.\n");
            }
        }
        // Do not close System.in scanner to avoid closing System.in for other uses
    }

    // Customer menu for authenticated customers
    private static void customerMenu(Scanner scanner, Account account) {
        boolean inCustomerMenu = true;
        while (inCustomerMenu) {
            System.out.println("\n=== Customer Menu ===");
            System.out.println("Account Holder: " + account.getAccountHolderName());
            System.out.println("Account Number: " + account.getAccountNumber());
            System.out.println("Current Balance: " + account.getBalance() + "\n");
            System.out.println("1. View Account Details");
            System.out.println("2. Deposit Money");
            System.out.println("3. Withdraw Money");
            System.out.println("4. View Transaction History");
            System.out.println("5. Download Transaction History");
            System.out.println("6. Logout");
            int choice = readInt(scanner, "Choose an option: ");

            switch (choice) {
                case 1: {
                    System.out.println("\n--- Account Details ---");
                    System.out.println("Account Number: " + account.getAccountNumber());
                    System.out.println("Account Holder: " + account.getAccountHolderName());
                    System.out.println("Current Balance: $" + account.getBalance());
                    System.out.println();
                    break;
                }
                case 2: {
                    double dep = readDouble(scanner, "Enter deposit amount: $");
                    account.deposit(dep);
                    System.out.println("Deposit successful. New balance: $" + account.getBalance() + "\n");
                    break;
                }
                case 3: {
                    double wd = readDouble(scanner, "Enter withdrawal amount: $");
                    if (account.withdraw(wd)) {
                        System.out.println("Withdrawal successful. New balance: $" + account.getBalance() + "\n");
                    } else {
                        System.out.println("Insufficient funds.\n");
                    }
                    break;
                }
                case 4: {
                    account.displayTransactionHistory();
                    break;
                }
                case 5: {
                    System.out.print("Enter filename to save (e.g., my_transactions.txt): ");
                    String filename = scanner.nextLine();
                    if (account.downloadTransactionHistory(filename)) {
                        System.out.println("Transaction history downloaded to: " + filename + "\n");
                    } else {
                        System.out.println("Failed to download transaction history.\n");
                    }
                    break;
                }
                case 6:
                    inCustomerMenu = false;
                    System.out.println("You have been logged out. Returning to main menu.\n");
                    break;
                default:
                    System.out.println("Invalid option. Please choose 1, 2, 3, 4, 5, or 6.\n");
            }
        }
    }

    // Admin menu for adding new users
    private static void adminMenu(Scanner scanner, Bank bank) {
        boolean inAdminMenu = true;
        while (inAdminMenu) {
            System.out.println("\nAdmin Menu:");
            System.out.println("1. Add New User");
            System.out.println("2. View All Accounts");
            System.out.println("3. Back to Main Menu");
            int choice = readInt(scanner, "Choose an option: ");

            switch (choice) {
                case 1: {
                    System.out.print("Enter new account number: ");
                    String accNum = scanner.nextLine();
                    System.out.print("Enter account holder name: ");
                    String name = scanner.nextLine();
                    double bal = readDouble(scanner, "Enter initial balance: ");
                    System.out.print("Enter password: ");
                    String pwd = scanner.nextLine();
                    
                    Account newAccount = new Account(accNum, name, bal, pwd);
                    bank.addAccount(newAccount);
                    System.out.println("New user account created successfully.\n");
                    break;
                }
                case 2: {
                    System.out.println("\n--- All Accounts ---");
                    for (Account a : bank.getAccounts()) {
                        System.out.println(a);
                    }
                    if (bank.getAccounts().isEmpty()) {
                        System.out.println("No accounts found.");
                    }
                    System.out.println();
                    break;
                }
                case 3:
                    inAdminMenu = false;
                    break;
                default:
                    System.out.println("Invalid option. Please choose 1, 2, or 3.\n");
            }
        }
    }
}
