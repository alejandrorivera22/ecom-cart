package com.alex.ecom_cart.infrastructure.services;

import com.alex.ecom_cart.api.dtos.response.CartProductResponse;
import com.alex.ecom_cart.api.dtos.response.CartResponse;
import com.alex.ecom_cart.domain.entities.*;
import com.alex.ecom_cart.domain.repositories.CartProductRepository;
import com.alex.ecom_cart.domain.repositories.CartRepository;
import com.alex.ecom_cart.domain.repositories.ProductRepository;
import com.alex.ecom_cart.domain.repositories.CustomerRepository;
import com.alex.ecom_cart.infrastructure.abstract_services.ICartService;
import com.alex.ecom_cart.util.enums.Tables;
import com.alex.ecom_cart.util.exceptions.IdNotFoundException;
import com.alex.ecom_cart.util.exceptions.ResourceNotEnabledException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CartServiceImpl implements ICartService {

    private final CartRepository cartRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final CartProductRepository cartProductRepository;

    @Override
    public CartResponse createCartForCustomer(Long customerId) {
        CustomerEntity customerFromDb = customerRepository.findById(customerId)
                .orElseThrow(() -> new IdNotFoundException(Tables.customer.name()));

        if (!customerFromDb.isEnabled()) {
            throw new ResourceNotEnabledException("Customer");
        }

        Optional<CartEntity> existingCart = cartRepository.findByCustomerId(customerId);
        if (existingCart.isPresent()) {
            return entityToResponse(existingCart.get());
        }

        CartEntity cartToSaved = CartEntity.builder()
                .customer(customerFromDb)
                .build();
        CartEntity cartSaved = this.cartRepository.save(cartToSaved);

        return entityToResponse(cartSaved);
    }

    @Override
    public CartResponse addProductToCart(Long cartId, Long productId, int quantity) {
        CartEntity cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IdNotFoundException(Tables.cart.name()));

        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new IdNotFoundException(Tables.product.name()));

        if (!product.isEnabled()) {
            throw new ResourceNotEnabledException(product.getName());
        }

        CartProductId id = new CartProductId(cartId, productId);
        CartProductEntity cartProduct = cartProductRepository.findById(id)
                .orElse(new CartProductEntity(cart, product, 0));

        cartProduct.setQuantity(cartProduct.getQuantity() + quantity);
        cartProductRepository.save(cartProduct);

        CartEntity updatedCart = cartRepository.findByIdWithProducts(cartId)
                .orElseThrow(() -> new IdNotFoundException(Tables.cart.name()));

        return entityToResponse(updatedCart);
    }

    @Override
    public CartResponse removeProductFromCart(Long cartId, Long productId) {
        CartProductId id = new CartProductId(cartId, productId);
        cartProductRepository.deleteById(id);

        CartEntity cart = cartRepository.findByIdWithProducts(cartId)
                .orElseThrow(() -> new IdNotFoundException(Tables.cart.name()));

        return entityToResponse(cart);
    }

    @Override
    public CartResponse getCartById(Long cartId) {
        CartEntity cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IdNotFoundException(Tables.cart.name()));
        return entityToResponse(cart);
    }

    @Override
    public CartResponse getCartByCustomerId(Long customerId) {
        CartEntity cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new IdNotFoundException("Cart not found for customer"));
        return entityToResponse(cart);
    }

    @Override
    public void clearCart(Long cartId) {
        cartProductRepository.deleteAllByCartId(cartId);
    }

    private CartResponse entityToResponse(CartEntity cart) {
        List<CartProductResponse> productResponses =
                (cart.getCartProducts() != null) ?
                cart.getCartProducts().stream()
                .map(cp -> CartProductResponse.builder()
                        .productId(cp.getProduct().getId())
                        .name(cp.getProduct().getName())
                        .price(cp.getProduct().getPrice())
                        .quantity(cp.getQuantity())
                        .build())
                .toList()
                : Collections.emptyList();

        return CartResponse.builder()
                .cartId(cart.getId())
                .customerId(cart.getCustomer().getId())
                .products(productResponses)
                .build();
    }
}
