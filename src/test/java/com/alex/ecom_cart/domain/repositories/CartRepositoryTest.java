package com.alex.ecom_cart.domain.repositories;

import com.alex.ecom_cart.domain.entities.CartEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CartRepositoryTest extends RepositorySpec{

    @Autowired
    private CartRepository cartRepository;

    @Test
    @DisplayName("findByCustomerId Should return a cart for a given customer ID")
    void shouldReturnCart_whenCustomerIdIsGiven() {
        Optional<CartEntity> result = this.cartRepository.findByCustomerId(1L);
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getCustomer().getId());
    }

    @Test
    @DisplayName("findByIdWithProducts Should return cart with associated products")
    void findByIdWithProducts() {
        // Act
        Optional<CartEntity> result = cartRepository.findByIdWithProducts(1L);

        // Assert
        assertTrue(result.isPresent(), "Cart should be present");
        CartEntity cart = result.get();
        assertNotNull(cart.getCartProducts());
        assertFalse(cart.getCartProducts().isEmpty(), "Cart should have products");

        cart.getCartProducts().forEach(cp -> {
            assertNotNull(cp.getProduct(), "Each CartProduct should have a Product");
            assertTrue(cp.getQuantity() > 0, "Product quantity should be greater than 0");
        });
    }
}