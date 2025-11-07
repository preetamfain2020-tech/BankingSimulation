package org.banking.dao;

import org.banking.model.Account;
import org.banking.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Optional;

public class AccountRepositoryImpl implements AccountRepository {

    private static final Logger logger = LoggerFactory.getLogger(AccountRepositoryImpl.class);

    @Override
    public void createAccount(Account account) {
        String sql = "INSERT INTO accounts (account_number, customer_id, account_type, balance, min_balance_threshold) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, account.getAccountNumber());
            pstmt.setInt(2, account.getCustomerId());
            pstmt.setString(3, account.getAccountType());
            pstmt.setBigDecimal(4, account.getBalance());
            pstmt.setBigDecimal(5, account.getMinBalanceThreshold());
            pstmt.executeUpdate();

            logger.info("✅ Account created successfully for Customer ID: {}", account.getCustomerId());

        } catch (SQLException e) {
            logger.error("❌ Error while creating account for Customer ID: {}", account.getCustomerId(), e);
        }
    }

    @Override
    public Optional<Account> findAccountByNumber(String accountNumber) {
        String sql = "SELECT * FROM accounts WHERE account_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, accountNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    logger.info("✅ Account found for Account Number: {}", accountNumber);
                    return Optional.of(mapRowToAccount(rs));
                }
            }

            logger.warn("⚠️ No account found for Account Number: {}", accountNumber);

        } catch (SQLException e) {
            logger.error("❌ Error fetching account with Account Number: {}", accountNumber, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Account> findAccountByCustomerId(int customerId) {
        String sql = "SELECT * FROM accounts WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    logger.info("✅ Account found for Customer ID: {}", customerId);
                    return Optional.of(mapRowToAccount(rs));
                }
            }

            logger.warn("⚠️ No account found for Customer ID: {}", customerId);

        } catch (SQLException e) {
            logger.error("❌ Error fetching account for Customer ID: {}", customerId, e);
        }
        return Optional.empty();
    }

    @Override
    public void updateAccountBalance(String accountNumber, BigDecimal newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE account_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBigDecimal(1, newBalance);
            pstmt.setString(2, accountNumber);
            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated > 0) {
                logger.info("✅ Account balance updated successfully for Account Number: {}", accountNumber);
            } else {
                logger.warn("⚠️ No account found for Account Number: {}", accountNumber);
            }

        } catch (SQLException e) {
            logger.error("❌ Error updating balance for Account Number: {}", accountNumber, e);
        }
    }

    @Override
    public Optional<Long> findLastAccountNumber() {
        String sql = "SELECT MAX(CAST(account_number AS UNSIGNED)) FROM accounts";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                long lastNumber = rs.getLong(1);
                if (lastNumber != 0) {
                    logger.info("✅ Last account number retrieved: {}", lastNumber);
                    return Optional.of(lastNumber);
                }
            }

            logger.warn("⚠️ No previous account numbers found.");

        } catch (SQLException e) {
            logger.error("❌ Error retrieving last account number.", e);
        }
        return Optional.empty();
    }

    // 🔹 Helper Method
    private Account mapRowToAccount(ResultSet rs) throws SQLException {
        Account account = new Account();
        account.setAccountNumber(rs.getString("account_number"));
        account.setCustomerId(rs.getInt("customer_id"));
        account.setAccountType(rs.getString("account_type"));
        account.setStatus(rs.getString("status"));
        account.setBalance(rs.getBigDecimal("balance"));
        account.setMinBalanceThreshold(rs.getBigDecimal("min_balance_threshold"));
        return account;
    }
}
