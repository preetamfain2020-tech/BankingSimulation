package org.banking.dao;

import org.banking.model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Hybrid repository:
 * - DB (AccountRepositoryImpl) is the source of truth.
 * - Frequently accessed accounts are cached in-memory (Collections).
 */
public class AccountRepositoryHybrid implements AccountRepository {

    private static final Logger logger = LoggerFactory.getLogger(AccountRepositoryHybrid.class);

    private final AccountRepositoryImpl dbRepo = new AccountRepositoryImpl(); // existing JDBC repo
    private final Map<String, Account> cache = new HashMap<>();               // collections-based cache

    @Override
    public void createAccount(Account account) {
        dbRepo.createAccount(account);
        cache.put(account.getAccountNumber(), account);
        logger.info("Account cached in memory: {}", account.getAccountNumber());
    }

    @Override
    public Optional<Account> findAccountByNumber(String accountNumber) {
        Account hit = cache.get(accountNumber);
        if (hit != null) {
            logger.info("Cache HIT for account {}", accountNumber);
            return Optional.of(hit);
        }
        logger.info("Cache MISS for account {}, loading from DB…", accountNumber);
        Optional<Account> dbAcc = dbRepo.findAccountByNumber(accountNumber);
        dbAcc.ifPresent(a -> cache.put(accountNumber, a));
        return dbAcc;
    }

    @Override
    public Optional<Account> findAccountByCustomerId(int customerId) {
        for (Account a : cache.values()) {
            if (a.getCustomerId() == customerId) {
                logger.info("Cache HIT for customer {}", customerId);
                return Optional.of(a);
            }
        }
        logger.info("Cache MISS for customer {}, loading from DB…", customerId);
        Optional<Account> dbAcc = dbRepo.findAccountByCustomerId(customerId);
        dbAcc.ifPresent(a -> cache.put(a.getAccountNumber(), a));
        return dbAcc;
    }

    @Override
    public void updateAccountBalance(String accountNumber, BigDecimal newBalance) {
        dbRepo.updateAccountBalance(accountNumber, newBalance);
        Account cached = cache.get(accountNumber);
        if (cached != null) {
            cached.setBalance(newBalance);
            logger.info("Cache updated for {}", accountNumber);
        }
    }

    @Override
    public Optional<Long> findLastAccountNumber() {
        return dbRepo.findLastAccountNumber();
    }
}
