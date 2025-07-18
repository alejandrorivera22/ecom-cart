package com.alex.ecom_cart.api.controllers.status400;

import com.alex.ecom_cart.DummyData;
import com.alex.ecom_cart.api.controllers.OrderController;
import com.alex.ecom_cart.api.dtos.request.OrderRequest;
import com.alex.ecom_cart.api.dtos.response.OrderResponse;
import com.alex.ecom_cart.config.security.SecurityConfig;
import com.alex.ecom_cart.infrastructure.abstract_services.IOrderService;
import com.alex.ecom_cart.infrastructure.services.security.UserDetailsServiceImpl;
import com.alex.ecom_cart.util.enums.OrderStatus;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.format.DateTimeFormatter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
class OrderControllerStatus400Test {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @MockitoBean
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Long INVALID_ORDER_ID = 1L;
    private static final String RESOURCE_PATH = "/order";
    private static final String USERNAME_ADMIN = "admin";
    private static final String ADMIN = "ADMIN";
    private static final String USERNAME_CUSTOMER = "cutomer";
    private static final String CUSTOMER = "CUSTOMER";
    private static final String USERNAME_SELLER = "seller";
    private static final String SELLER = "SELLER";

    @MockitoBean
    private IOrderService orderService;


    OrderRequest orderRequest;
    OrderResponse orderResponse;
    DateTimeFormatter formatter;

    @BeforeEach
    void setUp() {
        orderResponse = DummyData.createOrderResponse();
        orderRequest = DummyData.createOrderRequest();
        when(orderService.findById(INVALID_ORDER_ID)).thenThrow(new IdNotFoundException(Tables.order.name()));
    }

    @Test
    void findAll_ShouldReturn403_WhenCustomerNotAuthorized() throws Exception {
        mockMvc.perform(get(RESOURCE_PATH)
                        .param("field", "customer")
                        .param("desc", "true")
                        .param("page", "0"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return status 403 Forbidden")
    void createOrder_ShouldReturn403_WhenCustomerNotAuthorized() throws Exception {
        when(orderService.create(any(OrderRequest.class))).thenReturn(orderResponse);

        mockMvc.perform(post(RESOURCE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = USERNAME_ADMIN, roles = {ADMIN})
    @DisplayName("Should Return 400 when order ID does not exist")
    void getById_ShouldReturn400_WhenIdNotFound() throws Exception {
        String uri = RESOURCE_PATH + "/" + INVALID_ORDER_ID;
        mockMvc.perform(get(uri).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Id not found in order"));
    }

    @Test
    @WithMockUser(username = USERNAME_CUSTOMER, roles = {CUSTOMER})
    @DisplayName("Should Return 400 when customer ID does not exist")
    void getByCustomerId_ShouldReturn400_WhenIdNotFound() throws Exception {
        String uri = RESOURCE_PATH + "/" + "customer" + "/" + 1;
        when(orderService.findByCustomerId(1L)).thenThrow(new IdNotFoundException(Tables.customer.name()));

        mockMvc.perform(get(uri).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Id not found in customer"));
    }

    @Test
    @WithMockUser(username = USERNAME_SELLER, roles = SELLER)
    @DisplayName("Should Return 400 when STATUS is invalid")
    void updateStatus_ShouldReturn400() throws Exception {
        String uri = RESOURCE_PATH + "/" + "status-order" + "/" + 1;
        OrderStatus current = OrderStatus.PENDING;
        OrderStatus request = OrderStatus.COMPLETED;
        when(orderService.updateStatus(request, 1L))
                .thenThrow(new IllegalStateException(String.format("Cannot change state of %s to %s", current, request)));
        mockMvc.perform(patch(uri)
                        .param("status", "COMPLETED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Cannot change state of PENDING to COMPLETED"));
    }

    @Test
    @WithMockUser(username = USERNAME_SELLER, roles = {SELLER})
    @DisplayName("Should return status 403 Forbidden")
    void cancelOrder__ShouldReturn403_WhenCustomerNotAuthorized() throws Exception {
        String uri = RESOURCE_PATH + "/" + "cancel" + "/" + 1;

        mockMvc.perform(patch(uri)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = USERNAME_CUSTOMER, roles = {CUSTOMER})
    @DisplayName("Should Return 400 when STATUS is invalid")
    void cancelOrder_ShouldReturn400() throws Exception {
        String uri = RESOURCE_PATH + "/" + "cancel" + "/" + 1;
        OrderStatus current = OrderStatus.SHIPPED;
        OrderStatus request = OrderStatus.CANCELLED;
        when(orderService.cancelOrder(1L)).thenThrow(new IllegalStateException(String.format("Cannot change state of %s to %s", current, request)));
        mockMvc.perform(patch(uri)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Cannot change state of SHIPPED to CANCELLED"));
    }


}