package com.alex.ecom_cart.domain.repositories;

import com.alex.ecom_cart.domain.entities.CartProductEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class CartProductRepositoryTest extends RepositorySpec{

    @Autowired
    private CartProductRepository cartProductRepository;

    @Test
    @DisplayName("deleteAllByCartId should delete all products for a given cart ID")
    void deleteAllByCartId_shouldDeleteProductsForGivenCart() {
        Long cartId = 1L;

        List<CartProductEntity> beforeDelete = cartProductRepository.findAll();
        assertFalse(beforeDelete.isEmpty(), "Cart should have at least one product before delete");

        cartProductRepository.deleteAllByCartId(cartId);

        List<CartProductEntity> afterDelete = cartProductRepository.findAll();
        assertTrue(afterDelete.isEmpty(), "Cart products should be empty after delete");
    }
}