package com.alex.ecom_cart.infrastructure.abstract_services;

import com.alex.ecom_cart.api.dtos.request.OrderRequest;
import com.alex.ecom_cart.api.dtos.response.OrderResponse;
import com.alex.ecom_cart.util.enums.OrderStatus;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IOrderService{
    Page<OrderResponse> readAll(String field, Boolean desc, Integer page);
    OrderResponse create(OrderRequest request);
    OrderResponse findById(Long id);
    List<OrderResponse> findByCustomerId(Long customerId);
    OrderResponse updateStatus(OrderStatus orderStatus, Long id);
    OrderResponse cancelOrder(Long id);
}
