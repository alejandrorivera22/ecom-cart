package com.alex.ecom_cart.domain.repositories;

import com.alex.ecom_cart.domain.entities.OrderDetailEntity;
import com.alex.ecom_cart.domain.entities.OrderEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderDetailRepositoryTest extends RepositorySpec{

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Test
    @DisplayName("findByOrderId Should return all ordersDetails for a given order ID")
    void shouldReturnOrderDetails_whenOrderIdIsGiven() {
        List<OrderDetailEntity> result = this.orderDetailRepository.findByOrderId(1L);

        assertNotNull(result);

        int expectedElements = 2;
        assertEquals(expectedElements, result.size(), "Expected 2 elements");

       assertTrue(
               result.stream().allMatch(orderDetails -> orderDetails.getOrder().getId().equals(1L)),
               "product ID should be 1"
       );
    }

    @Test
    @DisplayName("findByProductId Should return all ordersDetails for a given product ID")
    void shouldReturnOrderDetails_whenProductIdIsGiven() {
        List<OrderDetailEntity> result = this.orderDetailRepository.findByProductId(2L);

        assertNotNull(result);

        int expectedElements = 1;
        assertEquals(expectedElements, result.size(), "Expected 1 elements");

        assertTrue(
                result.stream().allMatch(orderDetails -> orderDetails.getProduct().getId().equals(2L)),
                "product ID should be 2"
        );
    }
}