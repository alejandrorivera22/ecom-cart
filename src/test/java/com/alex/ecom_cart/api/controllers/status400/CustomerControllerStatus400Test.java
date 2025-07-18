package com.alex.ecom_cart.api.controllers.status400;

import com.alex.ecom_cart.DummyData;
import com.alex.ecom_cart.api.controllers.CustomerController;
import com.alex.ecom_cart.api.dtos.request.CustomerRequest;
import com.alex.ecom_cart.api.dtos.response.CustomerResponse;
import com.alex.ecom_cart.config.security.SecurityConfig;
import com.alex.ecom_cart.infrastructure.abstract_services.ICustomerService;
import com.alex.ecom_cart.infrastructure.services.security.UserDetailsServiceImpl;
import com.alex.ecom_cart.util.enums.Tables;
import com.alex.ecom_cart.util.exceptions.CustomerNotFoundException;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
@Import(SecurityConfig.class)
class CustomerControllerStatus400Test {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @MockitoBean
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Long INVAID_CUSTOMER_ID = 1L;
    private static final String RESOURCE_PATH = "/customer";
    private static final String ADMIN = "ADMIN";
    private static final String USERNAME_CUSTOMER = "CUSTOMER";
    private static final String USERNAME_ADMIN = "admin";
    private static final String SELLER = "SELLER";
    private static final String CUSTOMER = "CUSTOMER";

    @MockitoBean
    private ICustomerService customerService;


    CustomerResponse customerResponse;
    CustomerRequest customerRequest;

    @BeforeEach
    void setUp() {
        customerResponse = DummyData.createCustomerResponse();
        customerRequest = DummyData.createCustomerRequest();
        when(customerService.findById(INVAID_CUSTOMER_ID)).thenThrow(new IdNotFoundException(Tables.customer.name()));
    }

    @Test
    @WithMockUser(username = USERNAME_CUSTOMER, roles = {CUSTOMER})
    void findAll_ShouldReturn403_WhenCustomerNotAuthorized() throws Exception {
        mockMvc.perform(get(RESOURCE_PATH)
                        .param("field", "username")
                        .param("desc", "true")
                        .param("page", "0"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = USERNAME_CUSTOMER, roles = {CUSTOMER})
    @DisplayName("Should return status 403 Forbidden")
    void getById_ShouldReturn403_WhenCustomerNotAuthorized() throws Exception {
        String uri = RESOURCE_PATH + "/" + INVAID_CUSTOMER_ID;
        mockMvc.perform(get(uri).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = USERNAME_ADMIN, roles = {ADMIN})
    @DisplayName(" Shoud return 400 when customer ID does not exist")
    void getById_ShouldReturn400_WhenIdNotFound() throws Exception {
        String uri = RESOURCE_PATH + "/" + INVAID_CUSTOMER_ID;
        mockMvc.perform(get(uri).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Id not found in customer"));
    }

    @Test
    @WithMockUser(username = USERNAME_CUSTOMER, roles = {CUSTOMER})
    @DisplayName("Should return status 403 Forbidden")
    void getByUsername_ShouldReturn403_WhenCustomerNotAuthorized() throws Exception {
        String username = customerResponse.getUsername();
        String uri = RESOURCE_PATH + "/" + "username" + "/" + username;
        mockMvc.perform(get(uri).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = USERNAME_ADMIN, roles = {ADMIN})
    @DisplayName("Should Return 400 when username does not exist")
    void getByUsername_ShouldReturn400_WhenUsernameNotFound() throws Exception {
        String username = "InvalidUSername";
        when(this.customerService.findByUsername(username)).thenThrow(new CustomerNotFoundException("Username not found"));
        String uri = RESOURCE_PATH + "/" + "username" + "/" + username;
        mockMvc.perform(get(uri).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Username not found"));
    }

    @Test
    @WithMockUser(username = USERNAME_CUSTOMER, roles = {CUSTOMER})
    @DisplayName("Should return status 403 Forbidden")
    void getByEmail_ShouldReturn403_WhenCustomerNotAuthorized() throws Exception {
        String email = customerResponse.getEmail();
        String uri = RESOURCE_PATH + "/" + "email" + "/" + email;
        mockMvc.perform(get(uri).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(username = USERNAME_ADMIN, roles = {ADMIN})
    @DisplayName("Should Return 400 when emai does not exist")
    void getByEmail_ShouldReturn400_WhenEmaiNotFound() throws Exception {
        String email = customerResponse.getEmail();
        String uri = RESOURCE_PATH + "/" + "email" + "/" + email;
        when(this.customerService.findByEmail(email)).thenThrow(new CustomerNotFoundException("email not found"));
        mockMvc.perform(get(uri).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("email not found"));
    }

    @Test
    @WithMockUser(username = USERNAME_CUSTOMER, roles = {CUSTOMER})
    @DisplayName("Should return status 403 Forbidden")
    void getDisabledCustomers_ShouldReturn403_WhenCustomerNotAuthorized() throws Exception {
        String uri = RESOURCE_PATH + "/" + "disabled-customer";
        mockMvc.perform(get(uri).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return status 403 Forbidden")
    void updateCustomer_ShouldReturn403_WhenCustomerNotAuthorized() throws Exception {
        String uri = RESOURCE_PATH + "/" + INVAID_CUSTOMER_ID;
        CustomerRequest request = DummyData.createCustomerRequest();

        mockMvc.perform(put(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = USERNAME_ADMIN, roles = {ADMIN})
    @DisplayName("Should return 400 when request is invalid")
    void updateCustomer_ShouldReturn400_WhenInvalidRequest() throws Exception {
        CustomerRequest invalidRequest = new CustomerRequest();

        mockMvc.perform(put(RESOURCE_PATH + "/" + INVAID_CUSTOMER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("Should return status 403 Forbidden")
    void updateCustomerByUsername_ShouldReturn403_WhenCustomerNotAuthorized() throws Exception {
        String username = "usernameRequest";
        String uri = RESOURCE_PATH + "/" + "username" + "/" + username;
        CustomerRequest customerRequestUpdate = DummyData.createCustomerRequestUpdate();
        mockMvc.perform(put(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequestUpdate)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = USERNAME_ADMIN, roles = {ADMIN})
    @DisplayName("Should return status 400 when BAD REQUEST")
    void updateCustomerByUsername_ShouldReturn400_WhenInvalidRequest() throws Exception {
        String username = "usernameRequest";
        String uri = RESOURCE_PATH + "/" + "username" + "/" + username;
        CustomerRequest customerRequestUpdate = DummyData.createCustomerRequestUpdate();

        when(customerService.updateByUsername(any(CustomerRequest.class), eq(username)))
                .thenThrow(new CustomerNotFoundException("Username not found"));

        mockMvc.perform(put(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequestUpdate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Username not found"));
    }

    @Test
    @WithMockUser(username = USERNAME_ADMIN, roles = {ADMIN})
    @DisplayName("Should return 400 when adding role to non-existent user")
    void addRole_ShouldReturn400_WhenUsernameNotFound() throws Exception {
        String username = "nonexistent_user";
        when(customerService.addRole(eq(username), any())).thenThrow(new IdNotFoundException(Tables.customer.name()));

        mockMvc.perform(patch(RESOURCE_PATH + "/add-role")
                        .param("username", username)
                        .param("role", "SELLER"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Id not found in customer"))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("Should return status 403 Forbidden")
    void deleteCustomer_ShouldReturn403_WhenCustomerNotAuthorized() throws Exception {
        String uri = RESOURCE_PATH + "/" + INVAID_CUSTOMER_ID;
        mockMvc.perform(delete(uri))
                .andExpect(status().isForbidden());
    }
}