package org.banking.service;

import org.banking.dao.AccountRepository;
import org.banking.dao.CustomerRepository;
import org.banking.dao.TransactionRepository;
import org.banking.model.Account;
import org.banking.model.BankingSession;
import org.banking.model.Customer;
import org.banking.model.Transaction;
import org.banking.util.EmailService;
import org.banking.util.ReportGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public class BankingService {

    private static final Logger log = LoggerFactory.getLogger(BankingService.class);

    private final CustomerRepository customerRepo;
    private final AccountRepository accountRepo;
    private final TransactionRepository transactionRepo;

    public BankingService(CustomerRepository customerRepo,
                          AccountRepository accountRepo,
                          TransactionRepository transactionRepo) {
        this.customerRepo = customerRepo;
        this.accountRepo = accountRepo;
        this.transactionRepo = transactionRepo;
    }

    public Optional<BankingSession> login(String username, String password) {
        Optional<Customer> customerOpt = customerRepo.findCustomerByUsername(username);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            if (PasswordHasher.checkPassword(password, customer.getPasswordHash())) {
                Optional<Account> accountOpt = accountRepo.findAccountByCustomerId(customer.getCustomerId());
                if (accountOpt.isPresent()) {
                    return Optional.of(new BankingSession(customer, accountOpt.get()));
                }
            }
        }
        return Optional.empty();
    }

    public Account registerNewCustomerAndOpenAccount(Customer customer, String accountType,
                                                     BigDecimal initialDeposit) {
        customer.setPasswordHash(PasswordHasher.hashPassword(customer.getPasswordHash()));
        int customerId = customerRepo.createCustomer(customer);
        if (customerId == 0) return null;

        long nextAccNo = accountRepo.findLastAccountNumber().map(n -> n + 1).orElse(1000000001L);
        Account account = new Account();
        account.setAccountNumber(String.valueOf(nextAccNo));
        account.setCustomerId(customerId);
        account.setAccountType(accountType);
        account.setBalance(initialDeposit);

        BigDecimal minBalanceThreshold;
        switch (accountType.toLowerCase()) {
            case "savings" -> minBalanceThreshold = BigDecimal.valueOf(500);
            case "current" -> minBalanceThreshold = BigDecimal.valueOf(1000);
            default -> minBalanceThreshold = BigDecimal.valueOf(500);
        }
        account.setMinBalanceThreshold(minBalanceThreshold);

        accountRepo.createAccount(account);

        // DB log
        transactionRepo.saveTransaction(new Transaction(account.getAccountNumber(),
                "DEPOSIT", initialDeposit, new Timestamp(System.currentTimeMillis()), "Initial deposit"));

        // Text/file reports
        String holderName = holderName(account.getCustomerId());
        ReportGenerator.logTransaction(account.getAccountNumber(), "DEPOSIT", initialDeposit, initialDeposit);
        ReportGenerator.generateAccountSummary(holderName, account.getAccountNumber(), initialDeposit);

        return account;
    }

    public void deposit(String accountNumber, BigDecimal amount) {
        accountRepo.findAccountByNumber(accountNumber).ifPresent(account -> {
            BigDecimal newBalance = account.getBalance().add(amount);
            accountRepo.updateAccountBalance(accountNumber, newBalance);

            transactionRepo.saveTransaction(new Transaction(accountNumber,
                    "DEPOSIT", amount, new Timestamp(System.currentTimeMillis()), "Deposit"));

            String holderName = holderName(account.getCustomerId());
            ReportGenerator.logTransaction(accountNumber, "DEPOSIT", amount, newBalance);
            ReportGenerator.generateAccountSummary(holderName, accountNumber, newBalance);

            // If still below threshold after deposit, warn
            if (newBalance.compareTo(account.getMinBalanceThreshold()) < 0) {
                triggerLowBalanceAlerts(account, newBalance);
            }
        });
    }

    public TransactionOutcome withdraw(String accountNumber, BigDecimal amount) {
        Optional<Account> accountOpt = accountRepo.findAccountByNumber(accountNumber);
        if (accountOpt.isEmpty()) return TransactionOutcome.ACCOUNT_NOT_FOUND;

        Account account = accountOpt.get();
        BigDecimal availableFunds = account.getBalance().subtract(account.getMinBalanceThreshold());
        if (amount.compareTo(availableFunds) > 0) {
            // Notify for denied attempt due to min-balance policy
            notifyInsufficientBalanceAttempt(account, amount);
            return TransactionOutcome.INSUFFICIENT_BALANCE;
        }

        BigDecimal newBalance = account.getBalance().subtract(amount);
        accountRepo.updateAccountBalance(accountNumber, newBalance);

        transactionRepo.saveTransaction(new Transaction(accountNumber,
                "WITHDRAWAL", amount, new Timestamp(System.currentTimeMillis()), "Withdrawal"));

        String holderName = holderName(account.getCustomerId());
        ReportGenerator.logTransaction(accountNumber, "WITHDRAWAL", amount, newBalance);
        ReportGenerator.generateAccountSummary(holderName, accountNumber, newBalance);

        if (newBalance.compareTo(account.getMinBalanceThreshold()) < 0) {
            triggerLowBalanceAlerts(account, newBalance);
        }

        return TransactionOutcome.SUCCESSFUL;
    }

    public TransactionOutcome transfer(String fromAccount, String toAccount, BigDecimal amount) {
        Optional<Account> fromOpt = accountRepo.findAccountByNumber(fromAccount);
        Optional<Account> toOpt = accountRepo.findAccountByNumber(toAccount);
        if (fromOpt.isEmpty() || toOpt.isEmpty()) return TransactionOutcome.ACCOUNT_NOT_FOUND;

        Account from = fromOpt.get();
        Account to = toOpt.get();

        BigDecimal availableFunds = from.getBalance().subtract(from.getMinBalanceThreshold());
        if (amount.compareTo(availableFunds) > 0) {
            // Notify for denied attempt
            notifyInsufficientBalanceAttempt(from, amount);
            return TransactionOutcome.INSUFFICIENT_BALANCE;
        }

        BigDecimal fromNewBalance = from.getBalance().subtract(amount);
        BigDecimal toNewBalance   = to.getBalance().add(amount);

        accountRepo.updateAccountBalance(fromAccount, fromNewBalance);
        accountRepo.updateAccountBalance(toAccount, toNewBalance);

        transactionRepo.saveTransaction(new Transaction(fromAccount,
                "TRANSFER_OUT", amount, new Timestamp(System.currentTimeMillis()),
                "Transfer to " + toAccount));
        transactionRepo.saveTransaction(new Transaction(toAccount,
                "TRANSFER_IN", amount, new Timestamp(System.currentTimeMillis()),
                "Transfer from " + fromAccount));

        String fromName = holderName(from.getCustomerId());
        String toName   = holderName(to.getCustomerId());

        ReportGenerator.logTransaction(fromAccount, "TRANSFER_OUT", amount, fromNewBalance);
        ReportGenerator.logTransaction(toAccount, "TRANSFER_IN", amount, toNewBalance);
        ReportGenerator.generateAccountSummary(fromName, fromAccount, fromNewBalance);
        ReportGenerator.generateAccountSummary(toName, toAccount, toNewBalance);

        if (fromNewBalance.compareTo(from.getMinBalanceThreshold()) < 0) {
            triggerLowBalanceAlerts(from, fromNewBalance);
        }

        return TransactionOutcome.SUCCESSFUL;
    }

    public List<Transaction> getTransactionHistory(String accountNumber) {
        return transactionRepo.getTransactionsForAccount(accountNumber);
    }

    public boolean isUsernameTaken(String username) {
        return customerRepo.findCustomerByUsername(username).isPresent();
    }

    public boolean isEmailTaken(String email) {
        return customerRepo.findCustomerByEmail(email).isPresent();
    }

    public boolean isPhoneNumberTaken(String phone) {
        return customerRepo.findCustomerByPhoneNumber(phone).isPresent();
    }

    public Optional<Account> findAccount(String accountNumber) {
        return accountRepo.findAccountByNumber(accountNumber);
    }

    // ----------------- PRIVATE HELPERS -----------------

    private void triggerLowBalanceAlerts(Account account, BigDecimal newBalance) {
        BigDecimal thr = account.getMinBalanceThreshold();
        String accNo = account.getAccountNumber();
        String holderName = holderName(account.getCustomerId());

        // console/file alert
        ReportGenerator.alertLowBalance(holderName, accNo, newBalance, thr);

        // email alert
        customerRepo.findCustomerById(account.getCustomerId()).ifPresent(c -> {
            log.warn("ALERT: acc={} newBal={} thr={} → emailing {}", accNo, newBalance, thr, c.getEmail());
            EmailService.sendLowBalance(c.getEmail(), accNo, newBalance, thr);
        });
    }

    private void notifyInsufficientBalanceAttempt(Account account, BigDecimal amount) {
        String accNo = account.getAccountNumber();
        BigDecimal bal = account.getBalance();
        BigDecimal thr = account.getMinBalanceThreshold();
        String holderName = holderName(account.getCustomerId());

        // visible console note (and your existing logs will capture it)
        System.out.printf(
                "\n!!! INSUFFICIENT FUNDS ATTEMPT !!!\nAccount: %s\nHolder: %s\nTried: %s\nBalance: %s | Threshold: %s\n\n",
                accNo, holderName, amount.toPlainString(), bal.toPlainString(), thr.toPlainString()
        );

        // email polite denial notice
        customerRepo.findCustomerById(account.getCustomerId()).ifPresent(c -> {
            log.warn("DENIED: acc={} tried={} bal={} thr={} → emailing {}", accNo, amount, bal, thr, c.getEmail());
            EmailService.sendInsufficientFunds(c.getEmail(), accNo, bal, thr, amount);
        });
    }

    private String holderName(int customerId) {
        return customerRepo.findCustomerById(customerId)
                .map(c -> c.getFirstName() + " " + c.getLastName())
                .orElse("Customer-" + customerId);
    }
}
