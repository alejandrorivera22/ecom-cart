package com.alex.ecom_cart.domain.repositories;

import com.alex.ecom_cart.domain.entities.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<CartEntity, Long> {
    Optional<CartEntity> findByCustomerId(Long id);
    @Query("SELECT c FROM cart c LEFT JOIN FETCH c.cartProducts cp LEFT JOIN FETCH cp.product WHERE c.id = :id")
    Optional<CartEntity> findByIdWithProducts(@Param("id") Long id);
}
