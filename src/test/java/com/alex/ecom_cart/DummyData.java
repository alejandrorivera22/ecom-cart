package com.alex.ecom_cart;

import com.alex.ecom_cart.api.dtos.request.CustomerRequest;
import com.alex.ecom_cart.api.dtos.request.OrderProductRequest;
import com.alex.ecom_cart.api.dtos.request.OrderRequest;
import com.alex.ecom_cart.api.dtos.request.ProductRequest;
import com.alex.ecom_cart.api.dtos.response.CustomerResponse;
import com.alex.ecom_cart.api.dtos.response.OrderDetailResponse;
import com.alex.ecom_cart.api.dtos.response.OrderResponse;
import com.alex.ecom_cart.api.dtos.response.ProductResponse;
import com.alex.ecom_cart.domain.entities.*;
import com.alex.ecom_cart.util.enums.OrderStatus;
import com.alex.ecom_cart.util.enums.Role;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DummyData {
    //Products
    public static ProductRequest createProductRequest() {
        return ProductRequest.builder()
                .name("Dummy Laptop")
                .description("A high performance dummy laptop")
                .price(BigDecimal.valueOf(1500.00))
                .stock(10)
                .category(1L)
                .build();
    }

    public static ProductRequest createProductRequestUpdate() {
        return ProductRequest.builder()
                .name("Dummy Laptop update")
                .description("A high performance dummy laptop update")
                .price(BigDecimal.valueOf(1600.00))
                .stock(15)
                .category(1L)
                .build();
    }

    public static ProductResponse createUpdatedProductResponse() {
        return ProductResponse.builder()
                .id(1L)
                .name("Dummy Laptop update")
                .description("A high performance dummy laptop update")
                .price(BigDecimal.valueOf(1600.00))
                .stock(15)
                .category(1L)
                .build();
    }

    public static ProductEntity createProductEntity(CategoryEntity category) {
        return ProductEntity.builder()
                .id(1L)
                .name("Dummy Laptop")
                .description("A high performance dummy laptop")
                .price(BigDecimal.valueOf(1500.00))
                .stock(10)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .category(category)
                .orderDetails(new ArrayList<>())
                .cartProducts(new ArrayList<>())
                .build();
    }


    public static ProductEntity createProductEntityDisaled(CategoryEntity category) {
        return ProductEntity.builder()
                .id(1L)
                .name("Dummy Laptop")
                .description("A high performance dummy laptop")
                .price(BigDecimal.valueOf(1500.00))
                .stock(10)
                .enabled(false)
                .createdAt(LocalDateTime.now())
                .category(category)
                .build();
    }

    public static ProductResponse createProductResponse() {
        ProductResponse response = new ProductResponse();
        response.setId(1L);
        response.setName("Dummy Laptop");
        response.setDescription("A high performance dummy laptop");
        response.setPrice(BigDecimal.valueOf(1500.00));
        response.setStock(10);
        response.setCategory(1L);
        return response;
    }

    public static CategoryEntity createCategoryEntity() {
        return CategoryEntity.builder()
                .id(1L)
                .name("Electronics")
                .build();
    }

    public static List<ProductEntity> createProductEntityList(CategoryEntity category) {
        return List.of(
                createProductEntity(category),
                ProductEntity.builder()
                        .id(2L)
                        .name("Dummy Phone")
                        .description("Latest dummy smartphone")
                        .price(BigDecimal.valueOf(800.00))
                        .stock(20)
                        .enabled(true)
                        .createdAt(LocalDateTime.now())
                        .category(category)
                        .build()
        );
    }

    public static List<ProductResponse> createProductResponseList() {
        return List.of(
                createProductResponse(),
                ProductResponse.builder()
                        .id(2L)
                        .name("Dummy Phone")
                        .description("Latest dummy smartphone")
                        .price(BigDecimal.valueOf(800.00))
                        .stock(20)
                        .category(1L)
                        .build()
        );
    }

    //Customers
    public static CustomerRequest createCustomerRequest() {
        return CustomerRequest.builder()
                .username("dummy_user")
                .email("dummy_user@example.com")
                .password("password123")
                .build();
    }

    public static CustomerRequest createCustomerRequestUpdate() {
        return CustomerRequest.builder()
                .username("dummy_user_updated")
                .email("dummy_updated@example.com")
                .password("newPassword456")
                .build();
    }

    public static CustomerEntity createCustomerEntity(RoleEntity role) {
        return CustomerEntity.builder()
                .id(1L)
                .username("dummy_user")
                .email("dummy_user@example.com")
                .password("$2a$10$dummyencodedpassword")
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .roles(new HashSet<>(Set.of(role)))
                .build();
    }

    public static CustomerEntity createCustomerEntityDisabled(RoleEntity role) {
        return CustomerEntity.builder()
                .id(2L)
                .username("disabled_user")
                .email("disabled_user@example.com")
                .password("$2a$10$dummyencodedpassword")
                .enabled(false)
                .createdAt(LocalDateTime.now())
                .roles(Set.of(role))
                .build();
    }

    public static CustomerResponse createCustomerResponse() {
        return CustomerResponse.builder()
                .id(1L)
                .username("dummy_user")
                .email("dummy_user@example.com")
                .roles(new ArrayList<>(List.of("CUSTOMER")))
                .build();
    }

    public static CustomerResponse createUpdateCustomerResponse() {
        return CustomerResponse.builder()
                .username("dummy_user_updated")
                .email("dummy_updated@example.com")
                .roles(List.of("CUSTOMER"))
                .build();
    }

    public static List<CustomerResponse> createCustomerResponseList() {
        return List.of(createCustomerResponse(),
                CustomerResponse.builder()
                .id(2L)
                .username("dummy_user2")
                .email("dummy_user2@example.com")
                .roles(List.of("CUSTOMER"))
                .build());
    }

    public static RoleEntity createRoleEntityCustomer() {
        return RoleEntity.builder()
                .id((short) 2)
                .name(Role.CUSTOMER)
                .build();
    }

    public static RoleEntity createRoleEntitySeller() {
        return RoleEntity.builder()
                .id((short) 3)
                .name(Role.SELLER)
                .build();
    }

    public static List<CustomerEntity> createDisabledCustomerList() {
        RoleEntity role = createRoleEntityCustomer();
        return List.of(createCustomerEntityDisabled(role));
    }

    //Orders

    public static OrderEntity createOrderEntity(CustomerEntity customer, List<OrderDetailEntity> details) {
        return OrderEntity.builder()
                .id(1L)
                .customer(customer)
                .orderStatus(OrderStatus.PENDING)
                .totalPrice(BigDecimal.valueOf(850.00))
                .createdAt(LocalDateTime.now())
                .orderDetails(details)
                .build();
    }

    public static List<OrderProductRequest> createOrderProductRequest() {
        return new ArrayList<>(List.of(OrderProductRequest.builder()
                .productId(1L)
                .quantity(5)
                .build()));
    }

    public static OrderRequest createOrderRequest() {
        return OrderRequest.builder()
                .customerId(1L)
                .products(createOrderProductRequest())
                .build();
    }

    public static OrderResponse createOrderResponse() {
        return OrderResponse.builder()
                .id(1L)
                .username("dummy_user")
                .orderStatus(OrderStatus.PENDING)
                .totalPrice(BigDecimal.valueOf(100.50))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static OrderDetailEntity createOrderDetailEntity(OrderEntity order, ProductEntity product) {
        return OrderDetailEntity.builder()
                .id(1L)
                .product(product)
                .order(order)
                .quantity(2)
                .price(product.getPrice())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static OrderDetailResponse createOrderDetailResponse() {
        return OrderDetailResponse.builder()
                .id(1L)
                .order(1L)
                .price(BigDecimal.valueOf(100.50))
                .createdAt(LocalDateTime.now())
                .quantity(1)
                .product(1L)
                .build();

    }
}
