package com.alex.ecom_cart.domain.repositories;

import com.alex.ecom_cart.domain.entities.OrderEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderRepositoryTest extends RepositorySpec {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("findByCustomerId Should return all orders for a given customer ID")
    void shouldReturnOrder_whenCategoryIdIsGiven() {

        List<OrderEntity> result = this.orderRepository.findByCustomerId(1L);

        assertNotNull(result);

        int expectedElements = 1;
        assertEquals(expectedElements, result.size(), "Expected just 1 elemte");

        assertTrue(result.stream().allMatch(order -> order.getCustomer().getId().equals(1L)));

    }
}