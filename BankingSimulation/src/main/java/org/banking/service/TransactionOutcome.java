package org.banking.service;

public enum TransactionOutcome {
    SUCCESSFUL,
    INSUFFICIENT_BALANCE, // This will now mean "would go below min balance"
    ACCOUNT_NOT_FOUND
}