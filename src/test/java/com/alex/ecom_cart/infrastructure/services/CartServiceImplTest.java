package com.alex.ecom_cart.infrastructure.services;

import com.alex.ecom_cart.DummyData;
import com.alex.ecom_cart.api.dtos.response.CartResponse;
import com.alex.ecom_cart.domain.entities.CartEntity;
import com.alex.ecom_cart.domain.entities.CartProductEntity;
import com.alex.ecom_cart.domain.entities.CustomerEntity;
import com.alex.ecom_cart.domain.entities.ProductEntity;
import com.alex.ecom_cart.domain.repositories.CartProductRepository;
import com.alex.ecom_cart.domain.repositories.CartRepository;
import com.alex.ecom_cart.domain.repositories.CustomerRepository;
import com.alex.ecom_cart.domain.repositories.ProductRepository;
import com.alex.ecom_cart.util.exceptions.ResourceNotEnabledException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartServiceImplTest extends ServiceSpec{

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CartProductRepository cartProductRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private CustomerEntity customer;
    private ProductEntity product;
    private CartEntity cart;

    @BeforeEach
    void setUp() {
        customer = DummyData.createCustomerEntity(DummyData.createRoleEntityCustomer());
        product = DummyData.createProductEntity(DummyData.createCategoryEntity());

        cart = CartEntity.builder()
                .id(1L)
                .customer(customer)
                .cartProducts(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Shoul create a new cart for an enabled customer")
    void createCartForCustomer_ShouldCreateCart_WhenNotExists() {
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(customer.getId())).thenReturn(Optional.empty());
        when(cartRepository.save(any())).thenReturn(cart);

        CartResponse response = cartService.createCartForCustomer(customer.getId());

        assertNotNull(response);
        assertEquals(customer.getId(), response.getCustomerId());
        verify(cartRepository).save(any(CartEntity.class));
    }

    @Test
    @DisplayName("Shoul return existing cart for an enabled customer")
    void createCartForCustomer_ShouldReturnExistingCart_WhenExists() {
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(customer.getId())).thenReturn(Optional.of(cart));

        CartResponse response = cartService.createCartForCustomer(customer.getId());

        assertNotNull(response);
        assertEquals(customer.getId(), response.getCustomerId());
        verify(cartRepository, never()).save(any());
    }

    @Test
    @DisplayName("Shoul Throw ResourceNotEnabledException when customer not enabled")
    void createCartForCustomer_ShouldThrowResourceNotEnabledException_WhenCustomerDisabled() {
        customer.setEnabled(false);
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));

        assertThrows(ResourceNotEnabledException.class, () ->
                cartService.createCartForCustomer(customer.getId()));
    }

    @Test
    @DisplayName("Should add product to cart")
    void addProductToCart_ShouldAdd_WhenValid() {
        CartProductEntity cartProduct = new CartProductEntity(cart, product, 1);
        when(cartRepository.findById(cart.getId())).thenReturn(Optional.of(cart));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(cartProductRepository.findById(any())).thenReturn(Optional.of(cartProduct));
        when(cartProductRepository.save(any())).thenReturn(cartProduct);
        when(cartRepository.findByIdWithProducts(cart.getId())).thenReturn(Optional.of(cart));

        CartResponse response = cartService.addProductToCart(cart.getId(), product.getId(), 2);

        assertNotNull(response);
        verify(cartProductRepository).save(any(CartProductEntity.class));
    }

    @Test
    @DisplayName("Shoul Throw ResourceNotEnabledException when product not enabled")
    void addProductToCart_ShouldThrowResourceNotEnabledException_WhenProductDisabled() {
        product.setEnabled(false);
        when(cartRepository.findById(cart.getId())).thenReturn(Optional.of(cart));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        assertThrows(ResourceNotEnabledException.class, () ->
                cartService.addProductToCart(cart.getId(), product.getId(), 1));
    }

    @Test
    @DisplayName("Should remove product form cart")
    void removeProductFromCart_ShouldWork_WhenValid() {
        when(cartRepository.findByIdWithProducts(cart.getId())).thenReturn(Optional.of(cart));

        CartResponse response = cartService.removeProductFromCart(cart.getId(), product.getId());

        assertNotNull(response);
        verify(cartProductRepository).deleteById(any());
    }

    @Test
    @DisplayName("Should return cart for given valid Cart ID")
    void getCartById_ShouldReturnCart_WhenExists() {
        when(cartRepository.findById(cart.getId())).thenReturn(Optional.of(cart));

        CartResponse response = cartService.getCartById(cart.getId());

        assertNotNull(response);
        assertEquals(cart.getId(), response.getCartId());
    }

    @Test
    @DisplayName("Should return cart for given valid customer ID")
    void getCartByCustomerId_ShouldReturnCart_WhenExists() {
        when(cartRepository.findByCustomerId(customer.getId())).thenReturn(Optional.of(cart));

        CartResponse response = cartService.getCartByCustomerId(customer.getId());

        assertNotNull(response);
        assertEquals(customer.getId(), response.getCustomerId());
    }

    @Test
    @DisplayName("Should remove all products from a cart")
    void clearCart_ShouldDeleteAllProducts() {
        cartService.clearCart(cart.getId());
        verify(cartProductRepository).deleteAllByCartId(cart.getId());
    }
}
