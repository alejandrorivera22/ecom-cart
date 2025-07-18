package com.alex.ecom_cart.api.controllers.status200;

import com.alex.ecom_cart.DummyData;
import com.alex.ecom_cart.api.controllers.OrderController;
import com.alex.ecom_cart.api.dtos.request.OrderRequest;
import com.alex.ecom_cart.api.dtos.response.OrderResponse;
import com.alex.ecom_cart.config.security.SecurityConfig;
import com.alex.ecom_cart.infrastructure.abstract_services.IOrderService;
import com.alex.ecom_cart.infrastructure.services.security.UserDetailsServiceImpl;
import com.alex.ecom_cart.util.enums.OrderStatus;
import com.alex.ecom_cart.util.jwt.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
class OrderControllerStatus200Test {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @MockitoBean
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Long ORDER_ID = 1L;
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
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        when(orderService.findById(ORDER_ID)).thenReturn(orderResponse);
    }

    @Test
    @WithMockUser(username = "admin", roles = {ADMIN})
    void findAll() throws Exception {
        List<OrderResponse> orderResponseResponseList = List.of(orderResponse);
        Page<OrderResponse> page = new PageImpl<>(orderResponseResponseList);

        when(orderService.readAll("customer", true, 0)).thenReturn(page);

        mockMvc.perform(get(RESOURCE_PATH)
                        .param("field", "customer")
                        .param("desc", "true")
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(orderResponseResponseList.get(0).getId()))
                .andExpect(jsonPath("$.content[0].username").value(orderResponseResponseList.get(0).getUsername()))
                .andExpect(jsonPath("$.content[0].totalPrice").value(orderResponseResponseList.get(0).getTotalPrice()))
                .andExpect(jsonPath("$.content.length()").value(1));
        ;
    }

    @Test
    @WithMockUser(username = USERNAME_CUSTOMER, roles = {CUSTOMER})
    @DisplayName("Shoul create order when orderRequest is valid")
    void createOrder_ShouldReturnCreatedOrder() throws Exception {
        when(orderService.create(any(OrderRequest.class))).thenReturn(orderResponse);

        mockMvc.perform(post(RESOURCE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(orderResponse.getId()))
                .andExpect(jsonPath("$.totalPrice").value(orderResponse.getTotalPrice()))
                .andExpect(jsonPath("$.createdAt").value(orderResponse.getCreatedAt().format(formatter)))
                .andExpect(jsonPath("$.username").value(orderResponse.getUsername()));
    }

    @Test
    @WithMockUser(username = USERNAME_ADMIN, roles = {ADMIN})
    @DisplayName("Should return order given Order ID")
    void getById_ShouldReturnOrder() throws Exception {
        String uri = RESOURCE_PATH + "/" + ORDER_ID;
        mockMvc.perform(get(uri).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderResponse.getId()))
                .andExpect(jsonPath("$.totalPrice").value(orderResponse.getTotalPrice()))
                .andExpect(jsonPath("$.createdAt").value(orderResponse.getCreatedAt().format(formatter)))
                .andExpect(jsonPath("$.username").value(orderResponse.getUsername()));
    }

    @Test
    @WithMockUser(username = USERNAME_CUSTOMER, roles = {CUSTOMER})
    @DisplayName("Should return orders given customer ID")
    void getByCustomerId_ShouldReturnOrdersGivenCustomerId() throws Exception {
        String uri = RESOURCE_PATH + "/" + "customer" + "/" + 1;
        when(orderService.findByCustomerId(1L)).thenReturn(List.of(DummyData.createOrderResponse()));

        mockMvc.perform(get(uri).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(orderResponse.getId()))
                .andExpect(jsonPath("$[0].totalPrice").value(orderResponse.getTotalPrice()))
                .andExpect(jsonPath("$[0].createdAt").value(orderResponse.getCreatedAt().format(formatter)))
                .andExpect(jsonPath("$[0].username").value(orderResponse.getUsername()));
    }

    @Test
    @WithMockUser(username = USERNAME_SELLER, roles = SELLER)
    @DisplayName("Should return urderUpdated with new status")
    void updateStatus_ShouldReturnOrderResponse() throws Exception {
        String uri = RESOURCE_PATH + "/" + "status-order" + "/" + 1;
        OrderStatus status = OrderStatus.SHIPPED;
        when(orderService.updateStatus(status, 1L)).thenReturn(orderResponse);
        orderResponse.setOrderStatus(status);

        mockMvc.perform(patch(uri)
                        .param("status", "SHIPPED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("dummy_user"))
                .andExpect(jsonPath("$.orderStatus").value("SHIPPED"));
    }

    @Test
    @WithMockUser(username = USERNAME_CUSTOMER, roles = {CUSTOMER})
    @DisplayName("Should return order with CANCELLED status")
    void cancelOrder_ShouldCanceOrder() throws Exception {
        String uri = RESOURCE_PATH + "/" + "cancel" + "/" + 1;
        OrderStatus status = OrderStatus.CANCELLED;
        when(orderService.cancelOrder(1L)).thenReturn(orderResponse);
        orderResponse.setOrderStatus(status);
        mockMvc.perform(patch(uri)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("dummy_user"))
                .andExpect(jsonPath("$.orderStatus").value("CANCELLED"));
    }

}