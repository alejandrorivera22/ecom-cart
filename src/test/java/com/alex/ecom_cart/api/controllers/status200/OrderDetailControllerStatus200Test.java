package com.alex.ecom_cart.api.controllers.status200;

import com.alex.ecom_cart.DummyData;
import com.alex.ecom_cart.api.controllers.OrderDetailController;
import com.alex.ecom_cart.api.dtos.response.OrderDetailResponse;
import com.alex.ecom_cart.config.security.SecurityConfig;
import com.alex.ecom_cart.infrastructure.abstract_services.IOrderDetais;
import com.alex.ecom_cart.infrastructure.services.security.UserDetailsServiceImpl;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderDetailController.class)
@Import(SecurityConfig.class)
class OrderDetailControllerStatus200Test {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @MockitoBean
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String RESOURCE_PATH = "/order-detail";
    private static final String USERNAME_ADMIN = "admin";
    private static final String ADMIN = "ADMIN";
    private static final String USERNAME_CUSTOMER = "cutomer";
    private static final String CUSTOMER = "CUSTOMER";


    @MockitoBean
    private IOrderDetais orderDetailService;

    OrderDetailResponse orderDetailResponse;
    DateTimeFormatter formatter;

    @BeforeEach
    void setUp() {
        orderDetailResponse = DummyData.createOrderDetailResponse();
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    }

    @Test
    @WithMockUser(username = USERNAME_ADMIN, roles = ADMIN)
    void findAll() throws Exception {
        List<OrderDetailResponse> orderDetailResponseResponseList = List.of(orderDetailResponse);
        Page<OrderDetailResponse> page = new PageImpl<>(orderDetailResponseResponseList);

        when(orderDetailService.readAll("product", true, 0)).thenReturn(page);

        mockMvc.perform(get(RESOURCE_PATH)
                        .param("field", "product")
                        .param("desc", "true")
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(orderDetailResponseResponseList.get(0).getId()))
                .andExpect(jsonPath("$.content[0].order").value(orderDetailResponseResponseList.get(0).getOrder()))
                .andExpect(jsonPath("$.content[0].price").value(orderDetailResponseResponseList.get(0).getPrice()))
                .andExpect(jsonPath("$.content[0].product").value(orderDetailResponseResponseList.get(0).getProduct()))
                .andExpect(jsonPath("$.content[0].quantity").value(orderDetailResponseResponseList.get(0).getQuantity()))
                .andExpect(jsonPath("$.content[0].createdAt").value(orderDetailResponseResponseList.get(0).getCreatedAt().format(formatter)))
                .andExpect(jsonPath("$.content.length()").value(1));

    }

    @Test
    @WithMockUser(username = USERNAME_CUSTOMER, roles = CUSTOMER)
    @DisplayName("Should return all OrderDetails given a customer ID  valid")
    void findByOrderId() throws Exception {
        String uri = RESOURCE_PATH + "/" + "order" + "/" + 1;
        when(orderDetailService.findByOrderId(1L)).thenReturn(List.of(DummyData.createOrderDetailResponse()));

        mockMvc.perform(get(uri).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(orderDetailResponse.getId()))
                .andExpect(jsonPath("$[0].order").value(orderDetailResponse.getOrder()))
                .andExpect(jsonPath("$[0].price").value(orderDetailResponse.getPrice()))
                .andExpect(jsonPath("$[0].product").value(orderDetailResponse.getProduct()))
                .andExpect(jsonPath("$[0].quantity").value(orderDetailResponse.getQuantity()))
                .andExpect(jsonPath("$[0].createdAt").value(orderDetailResponse.getCreatedAt().format(formatter)))
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    @DisplayName("Should return all OrderDetails given a product ID valid")
    @WithMockUser(username = USERNAME_CUSTOMER, roles = CUSTOMER)
    void findByProductId() throws Exception {
        String uri = RESOURCE_PATH + "/" + "product" + "/" + 1;
        when(orderDetailService.findByProductId(1L)).thenReturn(List.of(DummyData.createOrderDetailResponse()));

        mockMvc.perform(get(uri).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(orderDetailResponse.getId()))
                .andExpect(jsonPath("$[0].order").value(orderDetailResponse.getOrder()))
                .andExpect(jsonPath("$[0].price").value(orderDetailResponse.getPrice()))
                .andExpect(jsonPath("$[0].product").value(orderDetailResponse.getProduct()))
                .andExpect(jsonPath("$[0].quantity").value(orderDetailResponse.getQuantity()))
                .andExpect(jsonPath("$[0].createdAt").value(orderDetailResponse.getCreatedAt().format(formatter)))
                .andExpect(jsonPath("$.size()").value(1));
    }
}