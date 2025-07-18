package com.alex.ecom_cart.domain.repositories;

import com.alex.ecom_cart.domain.entities.CustomerEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
class CustomerRepositoryTest extends RepositorySpec{

    @Autowired
    private CustomerRepository customerRepository;
    private static final String VALID_USERNAME = "john_doe";
    private static final String VALID_EMAIL = "john@example.com";

    @Test
    @DisplayName("findAllActiveCustomers should return only active customers")
    void shouldReturnCustomers_whenTheyAreActive() {
        Page<CustomerEntity> result = customerRepository.findAllActive(PageRequest.of(0, 5));

        assertNotNull(result, "The result should not be null");

        int expectedElements =  4;
        assertEquals(expectedElements, result.getTotalElements(), "Expected 4 enabled customers");

        for (CustomerEntity customer : result.getContent()) {
            assertTrue(customer.isEnabled(), "Found a customer that is disabled but expected all to be enabled");
        }
    }

    @Test
    @DisplayName("findAllByEnabledFalse should return only disabled customers")
    void shouldReturnCustomers_whenTheyAreDisbaled() {
        List<CustomerEntity> result = customerRepository.findAllByEnabledFalse();
        assertNotNull(result);
        int expected = 1;

        //validates the result size is 1
        assertEquals(expected, result.size(),"Expected exactly one disabled user");

        //validates that all users are disabled
        assertFalse(result.stream().allMatch(CustomerEntity::isEnabled), "Found a customer that is enabled but expected all to be disabled");

    }

    @Test
    @DisplayName("findByUsernameAndEnabledTrue should return enabled customer when username exists and is enabled")
    void shouldReturnCustomer_whenUsernameExistsAndIsEnabled() {

        Optional<CustomerEntity> customer = this.customerRepository.findByUsernameAndEnabledTrue(VALID_USERNAME);
        assertTrue(customer.isPresent(), "Customer shoul to be present");
        assertTrue(customer.get().isEnabled(), "Customer should be enabled");
        assertEquals(VALID_USERNAME, customer.get().getUsername(), "Customer username should be " + VALID_USERNAME);

    }

    @Test
    @DisplayName("findByUsername should return a customer when username exists")
    void shouldReturnCustomer_whenUsernameExists() {
        Optional<CustomerEntity> customer = this.customerRepository.findByUsername(VALID_USERNAME);
        assertTrue(customer.isPresent(), "Customer shoul to be present");
        assertEquals(VALID_USERNAME, customer.get().getUsername(), "Customer username should be " + VALID_USERNAME);
    }

    @Test
    @DisplayName("findByEmailAndEnabledTrue should return a customer when email exists and is enabled")
    void shouldReturnCustomer_EmailExistsAndIsEnabled() {
        Optional<CustomerEntity> customer = this.customerRepository.findByEmailAndEnabledTrue(VALID_EMAIL);
        assertTrue(customer.isPresent(), "Customer should to be present");
        assertTrue(customer.get().isEnabled(), "Customer should be enabled");
        assertEquals(VALID_EMAIL, customer.get().getEmail(), "Customer email should be " + VALID_EMAIL);
    }

    @Test
    @DisplayName("existsByUsername should return true when username exists")
    void shouldReturnTrue_whenUsernameExists() {
        boolean exists = customerRepository.existsByUsername(VALID_USERNAME);
        assertTrue(exists, "Should be true because username exists");
    }

    @Test
    @DisplayName("existsByUsername should return false when username does not exists")
    void shouldReturnFalse_whenUsernameDoesNotExists() {
        String invalidUsername = "invalid";
        boolean exists = customerRepository.existsByUsername(invalidUsername);
        assertFalse(exists, "Should be false because username does not exists");
    }

    @Test
    @DisplayName("existsByEmail should return true when email exists")
    void shouldReturnTrue_whenEmailExists() {
        boolean exists = customerRepository.existsByEmail(VALID_EMAIL);
        assertTrue(exists, "Should be true because email exists");
    }

    @Test
    @DisplayName("existsByUsername should return false when email does not exists")
    void shouldReturnFalse_whenEmailDoesNotExists() {
        String invalidEmail = "invalid";
        boolean exists = customerRepository.existsByEmail(invalidEmail);
        assertFalse(exists, "Should be false because email does not exists");
    }
}