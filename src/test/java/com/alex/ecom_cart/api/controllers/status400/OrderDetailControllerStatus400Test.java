package com.alex.ecom_cart.api.controllers.status400;

import com.alex.ecom_cart.DummyData;
import com.alex.ecom_cart.api.controllers.OrderDetailController;
import com.alex.ecom_cart.api.dtos.response.OrderDetailResponse;
import com.alex.ecom_cart.config.security.SecurityConfig;
import com.alex.ecom_cart.infrastructure.abstract_services.IOrderDetais;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.format.DateTimeFormatter;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderDetailController.class)
@Import(SecurityConfig.class)
class OrderDetailControllerStatus400Test {

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
    void findAll() throws Exception {
        mockMvc.perform(get(RESOURCE_PATH)
                        .param("field", "product")
                        .param("desc", "true")
                        .param("page", "0"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 403 forbiden")
    void findByOrderId() throws Exception {
        String uri = RESOURCE_PATH + "/" + "order" + "/" + 1;
        mockMvc.perform(get(uri).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should Return 400 when id does not exists")
    @WithMockUser(username = USERNAME_CUSTOMER, roles = CUSTOMER)
    void findByProductId_ShouldReturn400_WhenProductIdNOtFound() throws Exception {
        String uri = RESOURCE_PATH + "/" + "product" + "/" + 1;
        when(orderDetailService.findByProductId(1L)).thenThrow(new IdNotFoundException(Tables.product.name()));

        mockMvc.perform(get(uri).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Id not found in product"));
    }
}