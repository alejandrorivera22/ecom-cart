package com.alex.ecom_cart.domain.repositories;

import com.alex.ecom_cart.domain.entities.CartProductEntity;
import com.alex.ecom_cart.domain.entities.CartProductId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartProductRepository extends JpaRepository<CartProductEntity, CartProductId> {
    void deleteAllByCartId(Long cartId);
}
