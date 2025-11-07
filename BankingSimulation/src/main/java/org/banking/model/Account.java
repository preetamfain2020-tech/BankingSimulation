package org.banking.model;

import java.math.BigDecimal;

public class Account {
    private String accountNumber;
    private int customerId;
    private String accountType;
    private String status;
    private BigDecimal balance;
    private BigDecimal minBalanceThreshold; // New field

    // --- Getters and Setters ---
    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getMinBalanceThreshold() {
        return minBalanceThreshold;
    }

    public void setMinBalanceThreshold(BigDecimal minBalanceThreshold) {
        this.minBalanceThreshold = minBalanceThreshold;
    }

    @Override
    public String toString() {
        return "Account Details: {" +
                "Number='" + accountNumber + '\'' +
                ", Type='" + accountType + '\'' +
                ", Status='" + status + '\'' +
                ", Balance=" + String.format("%.2f", balance) +
                ", MinThreshold=" + String.format("%.2f", minBalanceThreshold) +
                '}';
    }
}