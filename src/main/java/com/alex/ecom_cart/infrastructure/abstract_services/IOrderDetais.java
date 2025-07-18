package com.alex.ecom_cart.infrastructure.abstract_services;

import com.alex.ecom_cart.api.dtos.response.OrderDetailResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IOrderDetais {

    Page<OrderDetailResponse> readAll(String field, Boolean desc, Integer page);
    List<OrderDetailResponse> findByOrderId(Long orderId);
    List<OrderDetailResponse> findByProductId(Long productId);

}
