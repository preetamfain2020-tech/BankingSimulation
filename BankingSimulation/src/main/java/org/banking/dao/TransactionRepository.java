package org.banking.dao;

import org.banking.model.Transaction;

import java.util.List;

public interface TransactionRepository {

    void saveTransaction(Transaction transaction);

    List<Transaction> getTransactionsForAccount(String accountNumber);
}
