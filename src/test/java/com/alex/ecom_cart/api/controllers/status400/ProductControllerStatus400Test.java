package com.alex.ecom_cart.api.controllers.status400;

import com.alex.ecom_cart.DummyData;
import com.alex.ecom_cart.api.controllers.ProductController;
import com.alex.ecom_cart.api.dtos.request.ProductRequest;
import com.alex.ecom_cart.api.dtos.response.ProductResponse;
import com.alex.ecom_cart.config.security.SecurityConfig;
import com.alex.ecom_cart.infrastructure.abstract_services.IProductService;
import com.alex.ecom_cart.infrastructure.services.security.UserDetailsServiceImpl;
import com.alex.ecom_cart.util.enums.Tables;
import com.alex.ecom_cart.util.exceptions.IdNotFoundException;
import com.alex.ecom_cart.util.exceptions.ResourceNotEnabledException;
import com.alex.ecom_cart.util.jwt.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
class ProductControllerStatus400Test {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @MockitoBean
    private IProductService productService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Long INVALID_PRODUCT_ID = 1L;
    private static final String RESOURCE_PATH = "/product";
    private static final String CUSTOMER = "CUSTOMER";

    ProductResponse productResponse;
    ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        productResponse = DummyData.createProductResponse();
        productRequest = DummyData.createProductRequest();
        when(productService.findById(INVALID_PRODUCT_ID)).thenThrow(new IdNotFoundException(Tables.product.name()));
    }

    @Test
    @WithMockUser(username = "customer", roles = {CUSTOMER})
    @DisplayName("Should return status 403 Forbidden")
    void createProduct_ShouldReturn403_WhenCustomerNotAuthorized() throws Exception {
        mockMvc.perform(post(RESOURCE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should Return 400 when product ID does not exist")
    void getById_ShouldReturn400_WhenIdNotFound() throws Exception {
        String uri = RESOURCE_PATH + "/" + INVALID_PRODUCT_ID;
        mockMvc.perform(get(uri).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Id not found in product"));
    }

    @Test
    @DisplayName("Should ShouldReturn400 when product is not enabled")
    void getById_ShouldReturn400_WhenProductDisabled() throws Exception {
        String uri = RESOURCE_PATH + "/" + INVALID_PRODUCT_ID;
        reset(productService);
        when(productService.findById(INVALID_PRODUCT_ID)).thenThrow(new ResourceNotEnabledException(Tables.product.name()));
        mockMvc.perform(get(uri).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("FORBIDDEN"))
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("Cannot proceed: product is not enabled"));
    }

    @Test
    @DisplayName("Should Return 400 when category ID does not exist")
    void getByCategoryId_ShouldReturn400_WhenIdNotFound() throws Exception {
        String uri = RESOURCE_PATH + "/" + "category" + "/" + INVALID_PRODUCT_ID;
        when(productService.findByCategoryId(INVALID_PRODUCT_ID))
                .thenThrow(new IdNotFoundException(Tables.category.name()));
        mockMvc.perform(get(uri).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Id not found in category"));
    }

    @Test
    @DisplayName("Should return empty list")
    void getDisabledProducts_ShouldReturnEmptyList() throws Exception {
        String uri = RESOURCE_PATH + "/" +  "disabled-products";
        when(productService.findDisabledProducts()).thenReturn(Collections.emptyList());

        mockMvc.perform(get(uri).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "customer", roles = {CUSTOMER})
    @DisplayName("Should return status 403 Forbidden")
    void update_ShouldReturn403_WhenCustomerNotAuthorized() throws Exception {
        String uri = RESOURCE_PATH + "/" + INVALID_PRODUCT_ID;
        ProductRequest request = DummyData.createProductRequestUpdate();

        mockMvc.perform(put(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(username = "customer", roles = {CUSTOMER})
    @DisplayName("Should return status 403 Forbidden")
    void updateStock_ShouldReturn403_WhenCustomerNotAuthorized() throws Exception {
        String uri = RESOURCE_PATH + "/" + INVALID_PRODUCT_ID + "/stock";
        Integer newStock = 30;

        mockMvc.perform(patch(uri)
                        .param("newStock", newStock.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "customer", roles = {CUSTOMER})
    @DisplayName("Should return status 403 Forbidden")
    void delete_ShouldReturn403_WhenCustomerNotAuthorized() throws Exception {
        String uri = RESOURCE_PATH + "/" + INVALID_PRODUCT_ID;
        mockMvc.perform(delete(uri))
                .andExpect(status().isForbidden());
    }

}