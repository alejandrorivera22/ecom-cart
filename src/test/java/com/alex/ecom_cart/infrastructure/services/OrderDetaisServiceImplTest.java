package com.alex.ecom_cart.infrastructure.services;

import com.alex.ecom_cart.DummyData;
import com.alex.ecom_cart.api.dtos.response.OrderDetailResponse;
import com.alex.ecom_cart.domain.entities.CustomerEntity;
import com.alex.ecom_cart.domain.entities.OrderDetailEntity;
import com.alex.ecom_cart.domain.entities.OrderEntity;
import com.alex.ecom_cart.domain.entities.ProductEntity;
import com.alex.ecom_cart.domain.repositories.OrderDetailRepository;
import com.alex.ecom_cart.domain.repositories.OrderRepository;
import com.alex.ecom_cart.domain.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;import static org.mockito.ArgumentMatchers.any;

class OrderDetaisServiceImplTest extends ServiceSpec{
    @Mock
    private OrderDetailRepository orderDetailRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderDetaisServiceImpl orderDetaisService;

    private OrderDetailEntity detail;
    private OrderEntity order;
    private ProductEntity product;
    private CustomerEntity customer;

    @BeforeEach
    void setUp() {
        customer = DummyData.createCustomerEntity(DummyData.createRoleEntityCustomer());
        product = DummyData.createProductEntity(DummyData.createCategoryEntity());
        order = DummyData.createOrderEntity(customer, List.of());

        detail = DummyData.createOrderDetailEntity(order, product);
    }
    @Test
    @DisplayName("Should return paged order detail responses sorted by price ascending")
    void readAll_ShouldReturnPagedOrderDetails() {
        List<OrderDetailEntity> detailList = List.of(detail);
        Pageable pageable = PageRequest.of(0, 5, Sort.by("product").ascending());
        Page<OrderDetailEntity> page = new PageImpl<>(detailList, pageable, detailList.size());

        when(orderDetailRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<OrderDetailResponse> resultPage = orderDetaisService.readAll("product", false, 0);

        assertNotNull(resultPage);
        assertEquals(1, resultPage.getTotalElements());

    }

    @Test
    @DisplayName("Should return order details list when order ID exists")
    void findByOrderId_ShouldReturnOrderDetailResponses() {
        ProductEntity product = DummyData.createProductEntity(DummyData.createCategoryEntity());
        OrderDetailEntity detail1 = DummyData.createOrderDetailEntity(order, product);
        OrderDetailEntity detail2 = DummyData.createOrderDetailEntity(order, product);
        Long orderId = 1L;

        when(orderRepository.existsById(orderId)).thenReturn(true);
        when(orderDetailRepository.findByOrderId(orderId)).thenReturn(List.of(detail1, detail2));

        List<OrderDetailResponse> responses = orderDetaisService.findByOrderId(orderId);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(detail1.getId(), responses.get(0).getId());
        assertEquals(detail2.getId(), responses.get(1).getId());

        verify(orderDetailRepository).findByOrderId(orderId);
    }

    @Test
    @DisplayName("Should return order details list when product ID exists")
    void findByProductId() {
        ProductEntity product = DummyData.createProductEntity(DummyData.createCategoryEntity());
        OrderDetailEntity detail = DummyData.createOrderDetailEntity(order, product);
        Long productId = product.getId();

        when(productRepository.existsById(productId)).thenReturn(true);
        when(orderDetailRepository.findByProductId(productId)).thenReturn(List.of(detail));

        List<OrderDetailResponse> responses = orderDetaisService.findByProductId(productId);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(detail.getId(), responses.get(0).getId());

        verify(orderDetailRepository).findByProductId(productId);
    }
}