package com.alex.ecom_cart.domain.repositories;

import com.alex.ecom_cart.domain.entities.CustomerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {

    @Query("SELECT p FROM customer p WHERE p.enabled = true")
    Page<CustomerEntity> findAllActive(Pageable pageable);
    List<CustomerEntity> findAllByEnabledFalse();
    Optional<CustomerEntity> findByUsernameAndEnabledTrue(String username);
    Optional<CustomerEntity> findByUsername(String username);
    Optional<CustomerEntity> findByEmailAndEnabledTrue(String email);
    boolean existsByUsername(String email);
    boolean existsByEmail(String email);

}

