package com.alex.ecom_cart.api.controllers;

import com.alex.ecom_cart.api.dtos.response.CartResponse;
import com.alex.ecom_cart.infrastructure.abstract_services.ICartService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final ICartService cartService;

    @Operation(summary = "Create a new cart for a customer")
    @PostMapping("/{customerId}")
    public ResponseEntity<CartResponse> createCartForCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(cartService.createCartForCustomer(customerId));
    }

    @Operation(summary = "Add a product to the customer cart")
    @PatchMapping("/{cartId}/product/{productId}")
    public ResponseEntity<CartResponse> addProductToCart(
            @PathVariable Long cartId,
            @PathVariable Long productId,
            @RequestParam int quantity
    ) {
        return ResponseEntity.ok(cartService.addProductToCart(cartId, productId, quantity));
    }

    @Operation(summary = "Remove a product from the customer cart")
    @DeleteMapping("/remove-product/{cartId}/{productId}")
    public ResponseEntity<CartResponse> removeProductFromCart(
            @PathVariable Long cartId,
            @PathVariable Long productId
    ) {
        return ResponseEntity.ok(cartService.removeProductFromCart(cartId, productId));
    }

    @Operation(summary = "Get a cart by ID")
    @GetMapping("/{cartId}")
    public ResponseEntity<CartResponse> getCartById(@PathVariable Long cartId) {
        return ResponseEntity.ok(cartService.getCartById(cartId));
    }

    @Operation(summary = "Get a cart by customer ID")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<CartResponse> getCartByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(cartService.getCartByCustomerId(customerId));
    }

    @Operation(summary = "Clear all products from the customer cart")
    @DeleteMapping("/{cartId}/clear")
    public ResponseEntity<Void> clearCart(@PathVariable Long cartId) {
        cartService.clearCart(cartId);
        return ResponseEntity.noContent().build();
    }

}
