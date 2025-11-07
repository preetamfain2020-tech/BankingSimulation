package org.banking.dao;

import org.banking.model.Customer;
import java.util.Optional;

public interface CustomerRepository {
    int createCustomer(Customer customer);
    Optional<Customer> findCustomerById(int customerId);
    Optional<Customer> findCustomerByUsername(String username);
    Optional<Customer> findCustomerByEmail(String email);
    Optional<Customer> findCustomerByPhoneNumber(String phoneNumber);
}