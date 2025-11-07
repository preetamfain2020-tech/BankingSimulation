package org.banking.dao;

import org.banking.model.Account;
import java.math.BigDecimal;
import java.util.Optional;

public interface AccountRepository {
    void createAccount(Account account);
    Optional<Account> findAccountByNumber(String accountNumber);
    void updateAccountBalance(String accountNumber, BigDecimal newBalance);
    Optional<Long> findLastAccountNumber();
    Optional<Account> findAccountByCustomerId(int customerId); // New
}