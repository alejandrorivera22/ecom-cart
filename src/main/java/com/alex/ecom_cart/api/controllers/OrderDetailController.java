package com.alex.ecom_cart.api.controllers;

import com.alex.ecom_cart.api.dtos.response.OrderDetailResponse;
import com.alex.ecom_cart.infrastructure.abstract_services.IOrderDetais;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order-detail")
@RequiredArgsConstructor
public class OrderDetailController {
    private final IOrderDetais orderDetais;

    @Operation(summary = "Retrieve all ordersDetails with pagination and optional sorting")
    @GetMapping()
    public ResponseEntity<Page<OrderDetailResponse>> findAll(
            @RequestParam(required = false, defaultValue = "product") String field,
            @RequestParam(required = false, defaultValue = "true") Boolean desc,
            @RequestParam(required = false, defaultValue = "0") Integer page) {
        Page<OrderDetailResponse> response = this.orderDetais.readAll(field, desc, page);
        return response.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }

    @Operation(summary = "Retrieve a OrderDetails list by orderId")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderDetailResponse>> findByOrderId(@PathVariable Long orderId){
        List<OrderDetailResponse> response = this.orderDetais.findByOrderId(orderId);
        return response.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }

    @Operation(summary = "Retrieve a OrderDetails list by productId")
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<OrderDetailResponse>> findByProductId(@PathVariable Long productId){
        List<OrderDetailResponse> response = this.orderDetais.findByProductId(productId);
        return response.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }

}
