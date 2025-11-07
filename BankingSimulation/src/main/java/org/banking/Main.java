package org.banking;

import org.banking.dao.*;
import org.banking.model.*;
import org.banking.service.BankingService;
import org.banking.service.TransactionOutcome;
import org.banking.util.BalanceAlertMonitor;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    private static final BankingService bankingService;
    private static final Scanner scanner = new Scanner(System.in);
    private static BankingSession currentSession = null;

    static {
        CustomerRepository customerRepository = new CustomerRepositoryImpl();
        AccountRepository accountRepository = new AccountRepositoryHybrid(); // Hybrid (DB + cache)
        TransactionRepository transactionRepository = new TransactionRepositoryImpl();
        bankingService = new BankingService(customerRepository, accountRepository, transactionRepository);
    }

    public static void main(String[] args) {
        System.out.println("\n--- WELCOME TO THE BANKING APP ---");


        // Start automatic balance-alert monitoring
        BalanceAlertMonitor.start();

        while (true) {
            if (currentSession == null) {
                displayMainMenu();
                handleMainMenuChoice();
            } else {
                displayAccountMenu();
                handleAccountMenuChoice();
            }
        }
    }

    private static void displayMainMenu() {
        System.out.println("\n1. Register");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        System.out.print("Enter choice: ");
    }

    private static void handleMainMenuChoice() {
        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid input. Enter a number.");
            return;
        }

        switch (choice) {
            case 1 -> register();
            case 2 -> login();
            case 3 -> System.exit(0);
            default -> System.out.println("❌ Invalid choice.");
        }
    }

    private static void displayAccountMenu() {
        System.out.println("\n1. Deposit");
        System.out.println("2. Withdraw");
        System.out.println("3. Transfer");
        System.out.println("4. Account Details");
        System.out.println("5. Transactions");
        System.out.println("6. Logout");
        System.out.print("Enter choice: ");
    }

    private static void handleAccountMenuChoice() {
        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid input. Enter a number.");
            return;
        }

        switch (choice) {
            case 1 -> deposit();
            case 2 -> withdraw();
            case 3 -> transfer();
            case 4 -> accountDetails();
            case 5 -> showTransactions();
            case 6 -> logout();
            default -> System.out.println("❌ Invalid choice.");
        }
    }

    private static void register() {
        try {
            Customer customer = new Customer();

            // Username
            System.out.print("Username: ");
            customer.setUsername(scanner.nextLine().trim());

            // Password
            System.out.print("Password: ");
            customer.setPasswordHash(scanner.nextLine().trim());

            // First Name
            while (true) {
                System.out.print("First Name: ");
                String firstName = scanner.nextLine().trim();
                if (firstName.matches("[a-zA-Z]+")) {
                    customer.setFirstName(firstName);
                    break;
                } else System.out.println("❌ First Name must contain only letters.");
            }

            // Last Name
            while (true) {
                System.out.print("Last Name: ");
                String lastName = scanner.nextLine().trim();
                if (lastName.matches("[a-zA-Z]+")) {
                    customer.setLastName(lastName);
                    break;
                } else System.out.println("❌ Last Name must contain only letters.");
            }

            // Email (improved regex + whitespace strip)
            while (true) {
                System.out.print("Email: ");
                String email = scanner.nextLine().trim().replaceAll("\\s+", "");
                if (isValidEmail(email)) {
                    customer.setEmail(email);
                    break;
                } else {
                    System.out.println("❌ Invalid email format.");
                }
            }

            // Phone
            while (true) {
                System.out.print("Phone (10 digits): ");
                String phone = scanner.nextLine().trim();
                if (phone.matches("\\d{10}")) {
                    customer.setPhoneNumber(phone);
                    break;
                } else System.out.println("❌ Phone number must be exactly 10 digits.");
            }

            // DOB
            while (true) {
                try {
                    System.out.print("DOB (YYYY-MM-DD): ");
                    customer.setDateOfBirth(Date.valueOf(scanner.nextLine().trim()));
                    break;
                } catch (IllegalArgumentException e) {
                    System.out.println("❌ Invalid date format.");
                }
            }

            // Address
            System.out.print("Address: "); customer.setAddress(scanner.nextLine().trim());
            System.out.print("City: "); customer.setCity(scanner.nextLine().trim());
            System.out.print("State: "); customer.setState(scanner.nextLine().trim());
            System.out.print("Postal Code: "); customer.setPostalCode(scanner.nextLine().trim());

            // Account Type (normalize to what service expects)
            String type;
            while (true) {
                System.out.print("Account Type (SAVINGS/CURRENT): ");
                type = scanner.nextLine().trim().toUpperCase();
                if (type.equals("SAVINGS") || type.equals("CURRENT")) break;
                else System.out.println("❌ Invalid type. Enter SAVINGS or CURRENT.");
            }
            String normalizedType = type.equals("SAVINGS") ? "savings" : "current";

            // Initial Deposit
            BigDecimal deposit;
            while (true) {
                try {
                    System.out.print("Initial Deposit: ");
                    deposit = new BigDecimal(scanner.nextLine().trim());
                    if (deposit.compareTo(BigDecimal.ZERO) >= 0) break;
                    else System.out.println("❌ Deposit cannot be negative.");
                } catch (NumberFormatException e) {
                    System.out.println("❌ Enter a valid number.");
                }
            }

            // Register
            Account acc = bankingService.registerNewCustomerAndOpenAccount(customer, normalizedType, deposit);
            if (acc != null) System.out.println("✅ Registration success! Account no: " + acc.getAccountNumber());
            else System.out.println("❌ Registration failed.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void login() {
        System.out.print("Username: ");
        String u = scanner.nextLine().trim();
        System.out.print("Password: ");
        String p = scanner.nextLine().trim();

        Optional<BankingSession> session = bankingService.login(u, p);
        if (session.isPresent()) {
            currentSession = session.get();
            System.out.println("✅ Login success!");
        } else System.out.println("❌ Invalid login.");
    }

    private static void deposit() {
        BigDecimal amt = getPositiveAmount("Enter amount to deposit: ");
        bankingService.deposit(currentSession.getAccount().getAccountNumber(), amt);
        refreshSessionAccount();
        System.out.println("✅ Deposit successful.");
    }

    private static void withdraw() {
        BigDecimal amt = getPositiveAmount("Enter amount to withdraw: ");
        TransactionOutcome res = bankingService.withdraw(currentSession.getAccount().getAccountNumber(), amt);
        refreshSessionAccount();
        switch (res) {
            case SUCCESSFUL -> System.out.println("✅ Withdrawal successful.");
            case INSUFFICIENT_BALANCE -> System.out.println("❌ Insufficient balance.");
            case ACCOUNT_NOT_FOUND -> System.out.println("❌ Account not found.");
        }
    }

    private static void transfer() {
        System.out.print("Recipient Account No: ");
        String to = scanner.nextLine().trim();
        BigDecimal amt = getPositiveAmount("Enter amount to transfer: ");
        TransactionOutcome res = bankingService.transfer(currentSession.getAccount().getAccountNumber(), to, amt);
        refreshSessionAccount();
        switch (res) {
            case SUCCESSFUL -> System.out.println("✅ Transfer successful.");
            case INSUFFICIENT_BALANCE -> System.out.println("❌ Insufficient balance.");
            case ACCOUNT_NOT_FOUND -> System.out.println("❌ Account not found.");
        }
    }

    private static void accountDetails() {
        refreshSessionAccount();
        Account a = currentSession.getAccount();
        Customer c = currentSession.getCustomer();
        System.out.println("\n--- ACCOUNT DETAILS ---");
        System.out.println("Holder: " + c.getFirstName() + " " + c.getLastName());
        System.out.println("Account No: " + a.getAccountNumber());
        System.out.println("Balance: " + a.getBalance());
        System.out.println("Minimum Threshold: " + a.getMinBalanceThreshold());
        BigDecimal available = a.getBalance().subtract(a.getMinBalanceThreshold());
        System.out.println("Available for Withdraw: " + (available.compareTo(BigDecimal.ZERO) < 0 ? 0 : available));
    }

    private static void showTransactions() {
        List<Transaction> txns = bankingService.getTransactionHistory(currentSession.getAccount().getAccountNumber());
        System.out.println("\n--- TRANSACTION HISTORY ---");
        if (txns.isEmpty()) {
            System.out.println("No transactions found.");
        } else {
            for (Transaction t : txns) {
                System.out.printf("%s | %s | %s | %s%n",
                        t.getTimestamp(),
                        t.getTransactionType(),
                        t.getAmount().toPlainString(),
                        t.getDescription());
            }
        }
    }

    private static void logout() {
        currentSession = null;
        System.out.println("✅ Logged out successfully.");
    }

    private static BigDecimal getPositiveAmount(String msg) {
        while (true) {
            try {
                System.out.print(msg);
                BigDecimal amt = new BigDecimal(scanner.nextLine().trim());
                if (amt.compareTo(BigDecimal.ZERO) < 0) System.out.println("❌ Amount must be positive.");
                else return amt;
            } catch (NumberFormatException e) {
                System.out.println("❌ Enter a valid number.");
            }
        }
    }

    private static void refreshSessionAccount() {
        if (currentSession == null) return;
        String accNo = currentSession.getAccount().getAccountNumber();
        bankingService.findAccount(accNo).ifPresent(a ->
                currentSession = new BankingSession(currentSession.getCustomer(), a)
        );
    }

    // improved email validation (no 6-char TLD cap)
    private static boolean isValidEmail(String s) {
        if (s == null) return false;
        s = s.trim();
        return s.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}
