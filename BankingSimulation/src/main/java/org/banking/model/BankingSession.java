package org.banking.model;

public class BankingSession {
    private final Customer customer;
    private final Account account;

    public BankingSession(Customer customer, Account account) {
        this.customer = customer;
        this.account = account;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Account getAccount() {
        return account;
    }
}