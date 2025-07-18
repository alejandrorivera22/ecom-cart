package com.alex.ecom_cart.api.controllers;

import com.alex.ecom_cart.api.dtos.request.OrderRequest;
import com.alex.ecom_cart.api.dtos.response.OrderResponse;
import com.alex.ecom_cart.infrastructure.abstract_services.IOrderService;
import com.alex.ecom_cart.util.enums.OrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final IOrderService orderService;

    @Operation(summary = "Retrieve all orders with pagination and optional sorting")
    @GetMapping()
    public ResponseEntity<Page<OrderResponse>> findAll(
            @RequestParam(required = false, defaultValue = "customer") String field,
            @RequestParam(required = false, defaultValue = "true") Boolean desc,
            @RequestParam(required = false, defaultValue = "0") Integer page) {

        Page<OrderResponse> response = this.orderService.readAll(field, desc, page);
        return response.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }

    @Operation(summary = "Create a new order for a customer")
    @PostMapping()
    public ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.orderService.create(request));
    }

    @Operation(summary = "Retrieve an order by ID")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long orderId){
        return ResponseEntity.ok(this.orderService.findById(orderId));
    }

    @Operation(summary = "Retrieve all orders placed by a customer")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getByCustomerId(@PathVariable Long customerId){
        return ResponseEntity.ok(this.orderService.findByCustomerId(customerId));
    }

    @Operation(summary = "Update the status of order")
    @PatchMapping("/status-order/{orderId}")
    public ResponseEntity<OrderResponse> updateStatus(@RequestParam OrderStatus status, @PathVariable Long orderId){
        return ResponseEntity.ok(this.orderService.updateStatus(status, orderId));
    }

    @Operation(summary = "Cancel an order by ID if it is not completed or shipped")
    @PatchMapping("/cancel/{orderId}")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long orderId){
        return ResponseEntity.ok(this.orderService.cancelOrder(orderId));
    }
}
