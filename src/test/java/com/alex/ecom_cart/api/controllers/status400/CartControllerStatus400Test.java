package com.alex.ecom_cart.api.controllers.status400;

import com.alex.ecom_cart.api.controllers.CartController;
import com.alex.ecom_cart.api.dtos.response.CartProductResponse;
import com.alex.ecom_cart.api.dtos.response.CartResponse;
import com.alex.ecom_cart.config.security.SecurityConfig;
import com.alex.ecom_cart.infrastructure.abstract_services.ICartService;
import com.alex.ecom_cart.infrastructure.services.security.UserDetailsServiceImpl;
import com.alex.ecom_cart.util.enums.Tables;
import com.alex.ecom_cart.util.exceptions.IdNotFoundException;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
@Import(SecurityConfig.class)
class CartControllerStatus400Test {

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
    @DisplayName("Should return status 403 Forbidden")
    void createCartForCustomer_ShouldReturn403_WhenCustomerNotAuthorized() throws Exception {
        String uri = RESOURCE_PATH + "/" + 1;
        mockMvc.perform(post(uri))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = USERNAME_CUSTOMER, roles = CUSTOMER)
    @DisplayName("Should return 400 when product ID not found")
    void addProductToCart_ShouldReturn400_WhenProductNotFound() throws Exception {
        String uri = RESOURCE_PATH + "/1/product/999";
        when(cartService.addProductToCart(1L, 999L, 1))
                .thenThrow(new IdNotFoundException(Tables.product.name()));

        mockMvc.perform(patch(uri).param("quantity", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Id not found in product"))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @WithMockUser(username = USERNAME_CUSTOMER, roles = CUSTOMER)
    @DisplayName("Should return 400 when cart ID not found")
    void getCartById_ShouldReturn400_WhenCartIdNotFound() throws Exception {
        String uri = RESOURCE_PATH + "/99";
        when(cartService.getCartById(99L)).thenThrow(new IdNotFoundException(Tables.cart.name()));

        mockMvc.perform(get(uri))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Id not found in cart"))
                .andExpect(jsonPath("$.code").value(400));
    }

}