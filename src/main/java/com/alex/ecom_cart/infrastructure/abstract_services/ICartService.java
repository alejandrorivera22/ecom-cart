package com.alex.ecom_cart.infrastructure.abstract_services;

import com.alex.ecom_cart.api.dtos.response.CartResponse;

public interface ICartService {
    CartResponse createCartForCustomer(Long customerId);
    CartResponse addProductToCart(Long cartId, Long productId, int quantity);
    CartResponse removeProductFromCart(Long cartId, Long productId);
    CartResponse getCartById(Long cartId);
    CartResponse getCartByCustomerId(Long customerId);
    void clearCart(Long cartId);
}
