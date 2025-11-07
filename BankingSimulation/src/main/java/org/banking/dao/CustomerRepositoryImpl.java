package org.banking.dao;

import org.banking.model.Customer;
import org.banking.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Optional;

public class CustomerRepositoryImpl implements CustomerRepository {

    private static final Logger logger = LoggerFactory.getLogger(CustomerRepositoryImpl.class);

    @Override
    public int createCustomer(Customer customer) {
        String sql = """
                INSERT INTO customers 
                (username, password_hash, first_name, last_name, date_of_birth, email, phone_number, address, city, state, postal_code) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        int customerId = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, customer.getUsername());
            pstmt.setString(2, customer.getPasswordHash());
            pstmt.setString(3, customer.getFirstName());
            pstmt.setString(4, customer.getLastName());
            pstmt.setDate(5, customer.getDateOfBirth());
            pstmt.setString(6, customer.getEmail());
            pstmt.setString(7, customer.getPhoneNumber());
            pstmt.setString(8, customer.getAddress());
            pstmt.setString(9, customer.getCity());
            pstmt.setString(10, customer.getState());
            pstmt.setString(11, customer.getPostalCode());

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        customerId = generatedKeys.getInt(1);
                        logger.info("✅ Customer created successfully: ID={}, Username={}", customerId, customer.getUsername());
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("❌ Error creating customer: {}", e.getMessage(), e);
        }

        return customerId;
    }

    @Override
    public Optional<Customer> findCustomerById(int customerId) {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    logger.info("🔍 Customer found with ID={}", customerId);
                    return Optional.of(mapRowToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("❌ Error finding customer by ID {}: {}", customerId, e.getMessage(), e);
        }
        logger.warn("⚠️ No customer found with ID={}", customerId);
        return Optional.empty();
    }

    @Override
    public Optional<Customer> findCustomerByUsername(String username) {
        return findCustomerByField("username", username);
    }

    @Override
    public Optional<Customer> findCustomerByEmail(String email) {
        return findCustomerByField("email", email);
    }

    @Override
    public Optional<Customer> findCustomerByPhoneNumber(String phoneNumber) {
        return findCustomerByField("phone_number", phoneNumber);
    }

    private Optional<Customer> findCustomerByField(String fieldName, String value) {
        String sql = "SELECT * FROM customers WHERE " + fieldName + " = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, value);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    logger.info("🔍 Customer found by {}={}", fieldName, value);
                    return Optional.of(mapRowToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("❌ Error finding customer by {}: {}", fieldName, e.getMessage(), e);
        }
        logger.warn("⚠️ No customer found by {}={}", fieldName, value);
        return Optional.empty();
    }

    private Customer mapRowToCustomer(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setCustomerId(rs.getInt("customer_id"));
        customer.setUsername(rs.getString("username"));
        customer.setPasswordHash(rs.getString("password_hash"));
        customer.setFirstName(rs.getString("first_name"));
        customer.setLastName(rs.getString("last_name"));
        customer.setDateOfBirth(rs.getDate("date_of_birth"));
        customer.setEmail(rs.getString("email"));
        customer.setPhoneNumber(rs.getString("phone_number"));
        customer.setAddress(rs.getString("address"));
        customer.setCity(rs.getString("city"));
        customer.setState(rs.getString("state"));
        customer.setPostalCode(rs.getString("postal_code"));
        return customer;
    }
}
