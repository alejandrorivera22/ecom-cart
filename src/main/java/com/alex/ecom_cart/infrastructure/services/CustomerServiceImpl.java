package com.alex.ecom_cart.infrastructure.services;

import com.alex.ecom_cart.api.dtos.request.CustomerRequest;
import com.alex.ecom_cart.api.dtos.response.CustomerResponse;
import com.alex.ecom_cart.config.RedisConfig;
import com.alex.ecom_cart.domain.entities.RoleEntity;
import com.alex.ecom_cart.domain.entities.CustomerEntity;
import com.alex.ecom_cart.domain.repositories.RoleRepositry;
import com.alex.ecom_cart.domain.repositories.CustomerRepository;
import com.alex.ecom_cart.infrastructure.abstract_services.ICustomerService;
import com.alex.ecom_cart.infrastructure.cache.CacheHelper;
import com.alex.ecom_cart.util.enums.Role;
import com.alex.ecom_cart.util.enums.Tables;
import com.alex.ecom_cart.util.exceptions.IdNotFoundException;
import com.alex.ecom_cart.util.exceptions.ResourceNotEnabledException;
import com.alex.ecom_cart.util.exceptions.CustomerNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements ICustomerService {

    private final CustomerRepository customerRepository;
    private final RoleRepositry roleRepositroy;
    private final static short PAGE_SIZE = 5;
    private static final Set<Role> VALID_ROLES = Set.of(Role.CUSTOMER, Role.ADMIN, Role.SELLER);
    private final CacheHelper cacheHelper;
    private final PasswordEncoder encoder;

    @Override
    public Page<CustomerResponse> readAll(String field, Boolean desc, Integer page) {

        Sort sorting = Sort.by("username");
        if (Objects.nonNull(field)){
            switch (field){
                case "username" -> sorting = Sort.by("username");
                case "email" -> sorting = Sort.by("email");
                default -> throw new IllegalArgumentException("Invalid field: " + field);
            }
        }
        Page<CustomerEntity> customers = desc
                ? this.customerRepository.findAllActive(PageRequest.of(page, PAGE_SIZE, sorting.descending()))
                : this.customerRepository.findAllActive(PageRequest.of(page, PAGE_SIZE, sorting.ascending()));
        return customers.map(this::entityToResponse);
    }

    @Override
    public CustomerResponse create(CustomerRequest request) {
        if (this.customerRepository.existsByUsername(request.getUsername())){
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }

        if (this.customerRepository.existsByEmail(request.getEmail())){
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        RoleEntity role = this.roleRepositroy.findByName(Role.CUSTOMER)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        CustomerEntity customerToPersist = CustomerEntity
                .builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .createdAt(LocalDateTime.now())
                .roles(Set.of(role))
                .enabled(true)
                .build();

        CustomerEntity customerPersisted = this.customerRepository.save(customerToPersist);

        CustomerResponse response = entityToResponse(customerPersisted);

        putCustomerCaches(customerPersisted, response);

        return response;
    }

    @Cacheable(value = RedisConfig.CacheConstants.CUSTOMERS_CACHE_NAME, key = "'customer:id:' + #id")
    @Override
    public CustomerResponse findById(Long id) {

        CustomerEntity customerFromDb = this.customerRepository.findById(id)
                .orElseThrow(() -> new IdNotFoundException(Tables.customer.name()));
        if (!customerFromDb.isEnabled()){
            throw new ResourceNotEnabledException(Tables.customer.name());
        }
        return this.entityToResponse(customerFromDb);
    }

    @Cacheable(value = RedisConfig.CacheConstants.CUSTOMERS_CACHE_NAME, key = "'customer:username:' + #username")
    @Override
    public CustomerResponse findByUsername(String username) {
        CustomerEntity customerFromDb = this.customerRepository.findByUsernameAndEnabledTrue(username)
                .orElseThrow(() -> new CustomerNotFoundException("Username not found"));
        return entityToResponse(customerFromDb);
    }

    @Cacheable(value = RedisConfig.CacheConstants.CUSTOMERS_CACHE_NAME, key = "'customer:email:' + #email")
    @Override
    public CustomerResponse findByEmail(String email) {
        CustomerEntity customerFromDb = this.customerRepository.findByEmailAndEnabledTrue(email)
                .orElseThrow(() -> new CustomerNotFoundException("email not found"));
        if (!customerFromDb.isEnabled()){
            throw new IdNotFoundException(Tables.customer.name());
        }
        return entityToResponse(customerFromDb);
    }

    @Cacheable(value = RedisConfig.CacheConstants.CUSTOMERS_CACHE_NAME)
    public List<CustomerResponse> findAllDisabledCustomers() {
        List<CustomerEntity> customersDisabled = this.customerRepository.findAllByEnabledFalse();
        return customersDisabled.stream()
                .map(this::entityToResponse)
                .toList();
    }

    public CustomerResponse update(CustomerRequest request, Long id) {
        CustomerEntity customerToUpdate = this.customerRepository.findById(id)
                .orElseThrow(() -> new IdNotFoundException(Tables.customer.name()));

        if (!customerToUpdate.isEnabled()){
            throw new ResourceNotEnabledException(Tables.customer.name());
        }

        evictCustomerCaches(customerToUpdate);
        customerToUpdate.setEmail(request.getEmail());
        customerToUpdate.setUsername(request.getUsername());
        customerToUpdate.setPassword(encoder.encode(request.getPassword()));

        CustomerEntity customerUpdated = this.customerRepository.save(customerToUpdate);
        CustomerResponse response = entityToResponse(customerUpdated);

        // Update cache manually with the 3 keys:
        putCustomerCaches(customerUpdated, response);

        return response;
    }

    public CustomerResponse updateByUsername(CustomerRequest request, String username) {
        CustomerEntity customerToUpdate = this.customerRepository.findByUsernameAndEnabledTrue(username)
                .orElseThrow(() -> new CustomerNotFoundException("Username not found"));

        evictCustomerCaches(customerToUpdate);

        customerToUpdate.setEmail(request.getEmail());
        customerToUpdate.setUsername(request.getUsername());
        customerToUpdate.setPassword(encoder.encode(request.getPassword()));

        CustomerEntity userUpdated = this.customerRepository.save(customerToUpdate);
        CustomerResponse response = entityToResponse(userUpdated);

        // Update cache manually with the 3 keys:
        putCustomerCaches(userUpdated, response);

        return response;
    }

    @Override
    public CustomerResponse addRole(String username, Role role) {

        if (!VALID_ROLES.contains(role)) {
            throw new IllegalArgumentException("Role does not exist: " + role.name());
        }

        CustomerEntity customerToUpdate = this.customerRepository.findByUsernameAndEnabledTrue(username)
                .orElseThrow(() -> new CustomerNotFoundException("Username not found"));

        RoleEntity roleEntity = this.roleRepositroy.findByName(role)
                .orElseThrow(() -> new IllegalArgumentException("Role not found in DB: " + role.name()));

        if (customerToUpdate.getRoles() == null) {
            customerToUpdate.setRoles(new HashSet<>());
        }

        customerToUpdate.getRoles().add(roleEntity);

        CustomerEntity customerUpdated = this.customerRepository.save(customerToUpdate);

        CustomerResponse response = entityToResponse(customerUpdated);

        // Update cache manually with the 3 keys:
        putCustomerCaches(customerUpdated, response);

        return response;
    }


    @CacheEvict(cacheNames = RedisConfig.CacheConstants.CUSTOMERS_CACHE_NAME, allEntries = true)
    @Override
    public void delete(Long id) {
        CustomerEntity customerToDelete = this.customerRepository.findById(id)
                .orElseThrow(() -> new IdNotFoundException(Tables.customer.name()));
        if (!customerToDelete.getOrders().isEmpty()){
            customerToDelete.disable();
            this.customerRepository.save(customerToDelete);
            this.customerRepository.flush();

            log.info("Customer with ID {} has orders. The user has been disabled instead of deleted.", customerToDelete.getId());
        } else {
            this.customerRepository.delete(customerToDelete);
        }
    }

    private CustomerResponse entityToResponse(CustomerEntity customerEntity){
        CustomerResponse response = new CustomerResponse();
        BeanUtils.copyProperties(customerEntity, response);
        response.setRoles(customerEntity.getRoles().stream().map(role -> role.getName().name()).toList());
        return response;
    }

    private void putCustomerCaches(CustomerEntity customer, CustomerResponse response) {
        String cacheName = RedisConfig.CacheConstants.CUSTOMERS_CACHE_NAME;
        cacheHelper.putCacheValues(cacheName, response,
                "customer:id:" + customer.getId(),
                "customer:username:" + customer.getUsername(),
                "customer:email:" + customer.getEmail()
        );
    }

    private void evictCustomerCaches(CustomerEntity customer) {
        String name = RedisConfig.CacheConstants.CUSTOMERS_CACHE_NAME;
        cacheHelper.evictCacheKeys(name,
                "customer:id:" + customer.getId(),
                "customer:username:" + customer.getUsername(),
                "customer:email:" + customer.getEmail());
    }

}
