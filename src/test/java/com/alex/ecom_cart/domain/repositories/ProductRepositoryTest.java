package com.alex.ecom_cart.domain.repositories;

import com.alex.ecom_cart.domain.entities.CustomerEntity;
import com.alex.ecom_cart.domain.entities.ProductEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
class ProductRepositoryTest extends RepositorySpec{

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("findAllActive should return only enabled products")
    void shouldReturnProducts_whenTheyAreActive() {
        Page<ProductEntity> result = productRepository.findAllActive(PageRequest.of(0, 5));

        assertNotNull(result, "The result should not be null");

        int expectedElements =  5;
        assertEquals(expectedElements, result.getTotalElements(), "Expected 5 enabled products");

        assertTrue(result.stream().allMatch(ProductEntity::isEnabled), "Found a product that is disabled but expected all to be enabled");

    }

    @Test
    @DisplayName("findByCategoryIdAndNotDeleted should return only active products for a given category id")
    void shouldReturnOnlyEnabledProducts_whenCategoryIdIsGiven() {
        List<ProductEntity> result = this.productRepository.findByCategoryIdAndNotDeleted(1L);
        assertNotNull(result, "The result should not be null");

        int expectedElements = 2;
        assertEquals(expectedElements, result.size(), "Expected 2 enabled products");

        assertTrue(result.stream().allMatch(ProductEntity::isEnabled), "products should be enabled");
        assertTrue(result.stream().allMatch(product -> product.getCategory().getId().equals(1L)),
                "Category id should be 1");

    }

    @Test
    @DisplayName("findAllByEnabledFalse should return only disabled products")
    void shouldReturnProducts_whenTheyAreDisabled() {
        List<ProductEntity> result = productRepository.findAllByEnabledFalse();

        assertNotNull(result, "The result should not be null");

        int expectedElements =  1;
        assertEquals(expectedElements, result.size(), "Expected 1 disabled products");

        assertFalse(result.stream().allMatch(ProductEntity::isEnabled), "products should be disabled");
    }
}