package com.alex.ecom_cart.api.controllers.status200;

import com.alex.ecom_cart.api.controllers.CartController;
import com.alex.ecom_cart.api.dtos.response.CartProductResponse;
import com.alex.ecom_cart.api.dtos.response.CartResponse;
import com.alex.ecom_cart.config.security.SecurityConfig;
import com.alex.ecom_cart.infrastructure.abstract_services.ICartService;
import com.alex.ecom_cart.infrastructure.services.security.UserDetailsServiceImpl;
import com.alex.ecom_cart.util.jwt.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@Import(SecurityConfig.class)
class CartControllerStatus200Test {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ICartService cartService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @MockitoBean
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private CartResponse cartResponse;

    private static final String RESOURCE_PATH = "/cart";
    private static final String USERNAME_CUSTOMER = "cutomer";
    private static final String CUSTOMER = "CUSTOMER";
    @BeforeEach
    void setUp() {
        cartResponse = CartResponse.builder()
                .cartId(1L)
                .customerId(1L)
                .products(List.of(CartProductResponse.builder()
                        .productId(1L)
                        .name("Mouse")
                        .price(BigDecimal.valueOf(29.99))
                        .quantity(1)
                        .build()))
                .build();
    }

    @Test
    @WithMockUser(username = USERNAME_CUSTOMER, roles = CUSTOMER)
    @DisplayName("Should create a cart for a customer")
    void createCartForCustomer() throws Exception {
        String uri = RESOURCE_PATH + "/" + 1;
        when(cartService.createCartForCustomer(1L)).thenReturn(cartResponse);

        mockMvc.perform(post(uri))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(cartResponse.getCartId()))
                .andExpect(jsonPath("$.customerId").value(cartResponse.getCustomerId()))
                .andExpect(jsonPath("$.products[0].productId").value(cartResponse.getProducts().get(0).getProductId()));
    }

    @Test
    @WithMockUser(username = USERNAME_CUSTOMER, roles = CUSTOMER)
    @DisplayName("Should add product to cart")
    void addProductToCart() throws Exception {
        String uri = RESOURCE_PATH + "/" + 1 + "/" + "product" + "/" + 1;
        when(cartService.addProductToCart(anyLong(), anyLong(), anyInt())).thenReturn(cartResponse);

        mockMvc.perform(patch(uri)
                        .param("quantity", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products[0].quantity").value(cartResponse.getProducts().get(0).getQuantity()))
                .andExpect(jsonPath("$.products[0].productId").value(cartResponse.getProducts().get(0).getProductId()));
    }

    @Test
    @WithMockUser(username = USERNAME_CUSTOMER, roles = CUSTOMER)
    @DisplayName("Should remove product from cart")
    void removeProductFromCart() throws Exception {
        String uri = RESOURCE_PATH + "/" + "remove-product" + "/" + 1 + "/" + 1;
        when(cartService.removeProductFromCart(1L, 1L)).thenReturn(cartResponse);

        mockMvc.perform(delete(uri))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1L))
                .andExpect(jsonPath("$.products[0].name").value("Mouse"));
    }

    @Test
    @WithMockUser(username = USERNAME_CUSTOMER, roles = CUSTOMER)
    @DisplayName("Should return cart by ID")
    void getCartById() throws Exception {
        String uri = RESOURCE_PATH + "/" + 1;
        when(cartService.getCartById(1L)).thenReturn(cartResponse);

        mockMvc.perform(get(uri))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(1L));
    }

    @Test
    @WithMockUser(username = USERNAME_CUSTOMER, roles = CUSTOMER)
    @DisplayName("Should return cart by customer ID")
    void getCartByCustomerId() throws Exception {
        String uri = RESOURCE_PATH + "/" + "customer" + "/" + 1;
        when(cartService.getCartByCustomerId(1L)).thenReturn(cartResponse);

        mockMvc.perform(get(uri))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1L));
    }

    @Test
    @WithMockUser(username = USERNAME_CUSTOMER, roles = CUSTOMER)
    @DisplayName("Should clear cart")
    void clearCart() throws Exception {
        String uri = RESOURCE_PATH + "/" + 1 + "/" + "clear";
        mockMvc.perform(delete("/cart/1/clear"))
                .andExpect(status().isNoContent());
    }
}