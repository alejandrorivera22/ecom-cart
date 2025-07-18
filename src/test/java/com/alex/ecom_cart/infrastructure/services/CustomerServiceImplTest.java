package com.alex.ecom_cart.infrastructure.services;

import com.alex.ecom_cart.DummyData;
import com.alex.ecom_cart.api.dtos.request.CustomerRequest;
import com.alex.ecom_cart.api.dtos.response.CustomerResponse;
import com.alex.ecom_cart.domain.entities.*;
import com.alex.ecom_cart.domain.repositories.CustomerRepository;
import com.alex.ecom_cart.domain.repositories.RoleRepositry;
import com.alex.ecom_cart.infrastructure.cache.CacheHelper;
import com.alex.ecom_cart.util.enums.Role;
import com.alex.ecom_cart.util.exceptions.CustomerNotFoundException;
import com.alex.ecom_cart.util.exceptions.IdNotFoundException;
import com.alex.ecom_cart.util.exceptions.ResourceNotEnabledException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CustomerServiceImplTest extends ServiceSpec {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private RoleRepositry roleRepositroy;
    @Mock
    private CacheHelper cacheHelper;
    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private CustomerServiceImpl customerService;

    CustomerEntity customer;
    RoleEntity role;
    Long customerId;

    @BeforeEach
    void setUp() {
        role = DummyData.createRoleEntityCustomer();
        customer = DummyData.createCustomerEntity(role);
        customerId = customer.getId();
    }

    @Test
    void readAll() {
        List<CustomerEntity> customerList = List.of(customer);

        Pageable pageable = PageRequest.of(0, 5, Sort.by("username").ascending());
        Page<CustomerEntity> customerPage = new PageImpl<>(customerList, pageable, customerList.size());

        when(customerRepository.findAllActive(any(PageRequest.class))).thenReturn(customerPage);

        Page<CustomerResponse> resultPage = customerService.readAll(null, false, 0);

        assertNotNull(resultPage);
        assertEquals(1, resultPage.getTotalElements());
        assertEquals(customer.getUsername(), resultPage.getContent().get(0).getUsername());
    }

    @Test
    @DisplayName("should create a customer and return a response")
    void create_ShouldSaveCustoomerAndReturnResponse() {
        CustomerRequest request = DummyData.createCustomerRequest();

        when(roleRepositroy.findByName(role.getName())).thenReturn(Optional.of(role));
        when(customerRepository.save(any(CustomerEntity.class))).thenReturn(customer);

        CustomerResponse response = customerService.create(request);

        assertNotNull(response);
        assertEquals("dummy_user", response.getUsername());
        verify(customerRepository).save(any(CustomerEntity.class));
    }

    @Test
    @DisplayName("happy path should return customer when it exists and is enabled")
    void findById_ShouldReturnCustomer_WhenEnabled() {

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        CustomerResponse response = customerService.findById(customerId);
        assertNotNull(response);

        assertAll(
                () -> assertEquals(customer.getId(), response.getId()),
                () -> assertEquals(customer.getUsername(), response.getUsername()),
                () -> assertEquals(customer.getEmail(), response.getEmail())
        );
    }

    @Test
    @DisplayName("Unhappy path Should throw ResourceNotEnabledException")
    void findById_ShouldThrowException_WhenCustomerNotEnabled() {
        CustomerEntity disabledCustomer = DummyData.createCustomerEntityDisabled(role);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(disabledCustomer));

        assertThrows(ResourceNotEnabledException.class, () -> customerService.findById(customerId));
    }

    @Test
    @DisplayName("happy path should return customer given username when it exists and is enabled")
    void findByUsername_ShouldReturnCustomer_WhenEnabled() {
        when(customerRepository.findByUsernameAndEnabledTrue(customer.getUsername())).thenReturn(Optional.of(customer));
        CustomerResponse response = customerService.findByUsername(customer.getUsername());
        assertNotNull(response);

        assertAll(
                () -> assertEquals(customer.getId(), response.getId()),
                () -> assertEquals(customer.getUsername(), response.getUsername()),
                () -> assertEquals(customer.getEmail(), response.getEmail())
        );
    }

    @Test
    @DisplayName("Unhappy path Should throw CustomerNotFoundException")
    void  findByUsername_ShouldThrowException_WhenCustomerNotFound() {
        when(customerRepository.findByUsernameAndEnabledTrue(anyString())).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.findByUsername("UsernameNotFound"));
    }


    @Test
    @DisplayName("happy path should return customer given email when it exists and is enabled")
    void findByEmail_ShouldReturnCustomer_WhenEnabled() {
        when(customerRepository.findByEmailAndEnabledTrue(customer.getEmail())).thenReturn(Optional.of(customer));
        CustomerResponse response = customerService.findByEmail(customer.getEmail());
        assertNotNull(response);

        assertAll(
                () -> assertEquals(customer.getId(), response.getId()),
                () -> assertEquals(customer.getUsername(), response.getUsername()),
                () -> assertEquals(customer.getEmail(), response.getEmail())
        );
    }

    @Test
    @DisplayName("should return all desabled customers")
    void findAllDisabledCustomers_ShouldReturnDisabledCustomers() {
        List<CustomerEntity> customers = DummyData.createDisabledCustomerList();

        when(customerRepository.findAllByEnabledFalse()).thenReturn(customers);
        List<CustomerResponse> response = customerService.findAllDisabledCustomers();

        assertNotNull(response);

        int expectedElements = 1;
        assertEquals(expectedElements, response.size());
    }

    @Test
    @DisplayName("Should update a customer given a valid request and customer ID")
    void update_ShouldReturnUpdateCustomer() {

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(CustomerEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerResponse response = customerService.findById(customerId);

        assertNotNull(response);
        assertAll(
                () -> assertEquals(customer.getId(), response.getId()),
                () -> assertEquals(customer.getUsername(), response.getUsername()),
                () -> assertEquals(customer.getEmail(), response.getEmail())
        );

        CustomerRequest updateRequest = DummyData.createCustomerRequestUpdate();
        CustomerResponse updateResponse = customerService.update(updateRequest, customerId);

        assertNotNull(updateResponse);
        assertAll(
                () -> assertEquals(customerId, updateResponse.getId()),
                () -> assertEquals(updateRequest.getUsername(), updateResponse.getUsername()),
                () -> assertEquals(updateRequest.getEmail(), updateResponse.getEmail())
        );
    }

    @Test
    @DisplayName("Should update a customer given a valid request and customer ID")
    void updateByUsername_ShouldReturnUpdateCustomer() {
        String username = customer.getUsername();
        when(customerRepository.findByUsernameAndEnabledTrue(username)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(CustomerEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerResponse response = customerService.findByUsername(username);

        assertNotNull(response);
        assertAll(
                () -> assertEquals(customer.getId(), response.getId()),
                () -> assertEquals(customer.getUsername(), response.getUsername()),
                () -> assertEquals(customer.getEmail(), response.getEmail())
        );

        CustomerRequest updateRequest = DummyData.createCustomerRequestUpdate();
        CustomerResponse updateResponse = customerService.updateByUsername(updateRequest, username);

        assertNotNull(updateResponse);
        assertAll(
                () -> assertEquals(customerId, updateResponse.getId()),
                () -> assertEquals(updateRequest.getUsername(), updateResponse.getUsername()),
                () -> assertEquals(updateRequest.getEmail(), updateResponse.getEmail())
        );
    }

    @Test
    @DisplayName("Should add role to customer when role and customer exist and are valid")
    void addRole_ShouldAddRoleToCustomer_WhenValid() {
        Role newRole = Role.SELLER;
        RoleEntity role = DummyData.createRoleEntitySeller();

        when(customerRepository.findByUsernameAndEnabledTrue(customer.getUsername()))
                .thenReturn(Optional.of(customer));

        when(roleRepositroy.findByName(newRole)).thenReturn(Optional.of(role));

        when(customerRepository.save(any(CustomerEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CustomerResponse response = customerService.addRole(customer.getUsername(), newRole);

        assertNotNull(response);
        assertTrue(response.getRoles().contains(newRole.name()));
    }

    @Test
    @DisplayName("happy path Should disable customer if it has orders instead of deleting")
    void delete_ShouldDisableCustomer_WhenHasOrders() {
        customer.setOrders(List.of(new OrderEntity())); // Simulamos que tiene órdenes
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        customerService.delete(customerId);

        assertFalse(customer.isEnabled());
        verify(customerRepository).save(customer);
        verify(customerRepository).flush();
    }

    @Test
    @DisplayName("happy path Should delete customer if no orders exist")
    void delete_ShouldDeleteCustomer_WhenNoOrders() {
       customer.setOrders(Collections.emptyList());
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        customerService.delete(customerId);

        verify(customerRepository).delete(customer);
    }

    @Test
    @DisplayName("Unhappy path Should throw idNotFoundException")
    void delete_ShouldThrowException_WhenCustomerIdNotExists() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        assertThrows(IdNotFoundException.class, () -> customerService.delete(customerId));
    }
}