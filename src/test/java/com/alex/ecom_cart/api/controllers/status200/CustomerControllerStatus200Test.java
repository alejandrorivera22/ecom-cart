package com.alex.ecom_cart.api.controllers.status200;

import com.alex.ecom_cart.DummyData;
import com.alex.ecom_cart.api.controllers.CustomerController;
import com.alex.ecom_cart.api.dtos.request.CustomerRequest;
import com.alex.ecom_cart.api.dtos.response.CustomerResponse;
import com.alex.ecom_cart.config.security.SecurityConfig;
import com.alex.ecom_cart.infrastructure.abstract_services.ICustomerService;
import com.alex.ecom_cart.infrastructure.services.security.UserDetailsServiceImpl;
import com.alex.ecom_cart.util.enums.Role;
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

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(CustomerController.class)
@Import(SecurityConfig.class)
class CustomerControllerStatus200Test {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @MockitoBean
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Long CUSTOMER_ID = 1L;
    private static final String RESOURCE_PATH = "/customer";
    private static final String ADMIN = "ADMIN";
    private static final String USERNAME_ADMIN = "ADMIN";
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
        when(customerService.findById(CUSTOMER_ID)).thenReturn(customerResponse);
    }
    
    @Test
    @WithMockUser(username = USERNAME_ADMIN, roles = {ADMIN})
    void findAll_ShouldReturnCustomers_WhenEnabled() throws Exception {
        List<CustomerResponse> customerResponseList = DummyData.createCustomerResponseList();
        Page<CustomerResponse> page = new PageImpl<>(customerResponseList);

        when(customerService.readAll("username", true, 0)).thenReturn(page);

        mockMvc.perform(get(RESOURCE_PATH)
                        .param("field", "username")
                        .param("desc", "true")
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value(customerResponseList.get(0).getUsername()))
                .andExpect(jsonPath("$.content[0].email").value(customerResponseList.get(0).getEmail()))
                .andExpect(jsonPath("$.content[1].username").value(customerResponseList.get(1).getUsername()))
                .andExpect(jsonPath("$.content[1].email").value(customerResponseList.get(1).getEmail()));
    }

    @Test
    @WithMockUser(username = USERNAME_ADMIN, roles = {ADMIN})
    @DisplayName("Should return customer when it exists and is enabled")
    void getById_ShouldReturnCustomer_WhenEnabled() throws Exception {
        String uri = RESOURCE_PATH + "/" + CUSTOMER_ID;
        mockMvc.perform(get(uri).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value(customerResponse.getUsername()))
                .andExpect(jsonPath("$.email").value(customerResponse.getEmail()))
                .andExpect(jsonPath("$.roles[0]").value(CUSTOMER));
    }

    @Test
    @WithMockUser(username = USERNAME_ADMIN, roles = {ADMIN})
    @DisplayName("Should return customer given valid username")
    void getByUsername_ShouldReturnCustomer_WhenEnabled() throws Exception {
        String username = customerResponse.getUsername();
        String uri = RESOURCE_PATH + "/" + "username" + "/" + username;
        when(customerService.findByUsername(username)).thenReturn(customerResponse);
        mockMvc.perform(get(uri).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value(customerResponse.getUsername()))
                .andExpect(jsonPath("$.email").value(customerResponse.getEmail()))
                .andExpect(jsonPath("$.roles[0]").value(CUSTOMER));
    }

    @Test
    @WithMockUser(username = USERNAME_ADMIN, roles = {ADMIN})
    @DisplayName("Should return customer given valid emai")
    void getByEmail_ShouldReturnCustomer_WhenEnabled() throws Exception {
        String email = customerResponse.getEmail();
        String uri = RESOURCE_PATH + "/" + "email" + "/" + email;
        when(customerService.findByEmail(email)).thenReturn(customerResponse);
        mockMvc.perform(get(uri).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value(customerResponse.getUsername()))
                .andExpect(jsonPath("$.email").value(customerResponse.getEmail()))
                .andExpect(jsonPath("$.roles[0]").value(CUSTOMER));
    }

    @Test
    @WithMockUser(username = USERNAME_ADMIN, roles = {ADMIN})
    @DisplayName("Should return all  disabed customers")
    void getDisabledCustomers_ShouldReturnAllDisabledCustomers() throws Exception {
        String uri = RESOURCE_PATH + "/" +  "disabled-customer";
        List<CustomerResponse> customerResponseList = DummyData.createCustomerResponseList();
        when(customerService.findAllDisabledCustomers()).thenReturn(customerResponseList);

        mockMvc.perform(get(uri).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].username").value(customerResponseList.get(0).getUsername()))
                .andExpect(jsonPath("$[0].email").value(customerResponseList.get(0).getEmail()))
                .andExpect(jsonPath("$[1].username").value(customerResponseList.get(1).getUsername()))
                .andExpect(jsonPath("$[1].email").value(customerResponseList.get(1).getEmail()));
    }

    @Test
    @WithMockUser(username = USERNAME_ADMIN, roles = {ADMIN})
    @DisplayName("Should update customer given a valid ID and request")
    void updateCustomer_ShouldReturnUpdatedCustomer_WhenValidRequestAndId() throws Exception {
        String uri = RESOURCE_PATH + "/" + CUSTOMER_ID;
        CustomerRequest customerRequestUpdate = DummyData.createCustomerRequestUpdate();
        CustomerResponse updatedResponse = DummyData.createUpdateCustomerResponse();

        when(customerService.update(any(CustomerRequest.class), eq(CUSTOMER_ID))).thenReturn(updatedResponse);

        mockMvc.perform(put(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequestUpdate)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value(updatedResponse.getUsername()))
                .andExpect(jsonPath("$.email").value(updatedResponse.getEmail()))
                .andExpect(jsonPath("$.roles[0]").value(CUSTOMER));
    }

    @Test
    @WithMockUser(username = USERNAME_ADMIN, roles = {ADMIN})
    @DisplayName("Should update customer given a valid username and request")
    void updateCustomerByUsername_ShouldReturnUpdatedCustomer_WhenValidRequestAndId() throws Exception {
        String username = "usernameRequest";
        String uri = RESOURCE_PATH + "/" + "username" + "/" + username ;
        CustomerRequest customerRequestUpdate = DummyData.createCustomerRequestUpdate();
        CustomerResponse updatedResponse = DummyData.createUpdateCustomerResponse();

        when(customerService.updateByUsername(any(CustomerRequest.class), eq(username))).thenReturn(updatedResponse);

        mockMvc.perform(put(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequestUpdate)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value(updatedResponse.getUsername()))
                .andExpect(jsonPath("$.email").value(updatedResponse.getEmail()))
                .andExpect(jsonPath("$.roles[0]").value(CUSTOMER));
    }

    @Test
    @WithMockUser(username = USERNAME_ADMIN, roles = {ADMIN})
    @DisplayName("Should add role to customer given valid username and role")
    void addRole_ShouldAddRoleToCustomerAndRuturn() throws Exception {
        String uri = RESOURCE_PATH + "/" + "add-role";
        Role role = Role.SELLER;
        customerResponse.getRoles().add(role.name());
        when(this.customerService.addRole(anyString(), any(Role.class))).thenReturn(customerResponse);

        mockMvc.perform(patch(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("username", "dummy_user")
                        .param("role", "SELLER"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value(customerResponse.getUsername()))
                .andExpect(jsonPath("$.email").value(customerResponse.getEmail()))
                .andExpect(jsonPath("$.roles[0]").value(CUSTOMER))
                .andExpect(jsonPath("$.roles[1]").value(SELLER));
    }

    @Test
    @WithMockUser(username = USERNAME_ADMIN, roles = {ADMIN})
    @DisplayName("Should delete customer when ID exists")
    void deleteCustomer() throws Exception {
        String uri = RESOURCE_PATH + "/" + CUSTOMER_ID;
        mockMvc.perform(delete(uri))
                .andExpect(status().isNoContent());

        verify(customerService, times(1)).delete(eq(CUSTOMER_ID));
    }
}