package com.alex.ecom_cart.api.controllers.status200;

import com.alex.ecom_cart.DummyData;
import com.alex.ecom_cart.api.controllers.ProductController;
import com.alex.ecom_cart.api.dtos.request.ProductRequest;
import com.alex.ecom_cart.api.dtos.response.ProductResponse;
import com.alex.ecom_cart.config.security.SecurityConfig;
import com.alex.ecom_cart.infrastructure.abstract_services.IProductService;
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

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
class ProductControllerStatus200Test {
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

    private static final Long PRODUCT_ID = 1L;
    private static final String RESOURCE_PATH = "/product";
    private static final String ADMIN = "ADMIN";
    private static final String SELLER = "SELLER";

    ProductResponse productResponse;
    ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        productResponse = DummyData.createProductResponse();
        productRequest = DummyData.createProductRequest();
        when(productService.findById(PRODUCT_ID)).thenReturn(productResponse);
    }

    @Test
    void findAll_ShouldReturnProducts_WhenProductsExist() throws Exception {
        List<ProductResponse> productResponseList = DummyData.createProductResponseList();
        Page<ProductResponse> page = new PageImpl<>(productResponseList);

        when(productService.readAll("name", true, 0)).thenReturn(page);

        mockMvc.perform(get(RESOURCE_PATH)
                        .param("field", "name")
                        .param("desc", "true")
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value(productResponseList.get(0).getName()))
                .andExpect(jsonPath("$.content[0].description").value(productResponseList.get(0).getDescription()))
                .andExpect(jsonPath("$.content[0].price").value(productResponseList.get(0).getPrice()))
                .andExpect(jsonPath("$.content[0].stock").value(productResponseList.get(0).getStock()))
                .andExpect(jsonPath("$.content[0].category").value(productResponseList.get(0).getCategory()))
                .andExpect(jsonPath("$.content[1].name").value(productResponseList.get(1).getName()))
                .andExpect(jsonPath("$.content[1].description").value(productResponseList.get(1).getDescription()))
                .andExpect(jsonPath("$.content[1].price").value(productResponseList.get(1).getPrice()))
                .andExpect(jsonPath("$.content[1].stock").value(productResponseList.get(1).getStock()))
                .andExpect(jsonPath("$.content[1].category").value(productResponseList.get(1).getCategory()));
    }

    @Test
    @WithMockUser(username = "admin", roles = {ADMIN, SELLER})
    @DisplayName("Shoul create Product when productRequest is valid")
    void createProduct_ShouldReturnCreatedProduct() throws Exception {
        when(productService.create(any(ProductRequest.class))).thenReturn(productResponse);

        mockMvc.perform(post(RESOURCE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(productResponse.getId()))
                .andExpect(jsonPath("$.name").value(productResponse.getName()))
                .andExpect(jsonPath("$.price").value(productResponse.getPrice()));
    }

    @Test
    @DisplayName("Should return product when it exists and is enabled")
    void getById_ShouldReturnProduct_WhenEnabled() throws Exception {
        String uri = RESOURCE_PATH + "/" + PRODUCT_ID;
        mockMvc.perform(get(uri).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(productResponse.getName()))
                .andExpect(jsonPath("$.description").value(productResponse.getDescription()))
                .andExpect(jsonPath("$.price").value(productResponse.getPrice()))
                .andExpect(jsonPath("$.stock").value(productResponse.getStock()))
                .andExpect(jsonPath("$.category").value(productResponse.getCategory()));
    }

    @Test
    @DisplayName("Should return all products given a valid category ID")
    void getByCategoryId_ShouldReturnProducts_WhenCategoryIdExists() throws Exception {
        String uri = RESOURCE_PATH + "/" + "category" + "/" + 1;
        List<ProductResponse> productResponseList = DummyData.createProductResponseList();
        when(productService.findByCategoryId(1L)).thenReturn(productResponseList);

        mockMvc.perform(get(uri).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value(productResponseList.get(0).getName()))
                .andExpect(jsonPath("$[0].description").value(productResponseList.get(0).getDescription()))
                .andExpect(jsonPath("$[0].price").value(productResponseList.get(0).getPrice()))
                .andExpect(jsonPath("$[0].stock").value(productResponseList.get(0).getStock()))
                .andExpect(jsonPath("$[0].category").value(productResponseList.get(0).getCategory()))
                .andExpect(jsonPath("$[1].name").value(productResponseList.get(1).getName()))
                .andExpect(jsonPath("$[1].description").value(productResponseList.get(1).getDescription()))
                .andExpect(jsonPath("$[1].price").value(productResponseList.get(1).getPrice()))
                .andExpect(jsonPath("$[1].stock").value(productResponseList.get(1).getStock()))
                .andExpect(jsonPath("$[1].category").value(productResponseList.get(1).getCategory()));

    }

    @Test
    @DisplayName("Should return all  disabed products")
    void getDisabledProducts_ShouldReturnAllDisabledProducts() throws Exception {
        String uri = RESOURCE_PATH + "/" +  "disabled-products";
        List<ProductResponse> productResponseList = DummyData.createProductResponseList();
        when(productService.findDisabledProducts()).thenReturn(productResponseList);

        mockMvc.perform(get(uri).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value(productResponseList.get(0).getName()))
                .andExpect(jsonPath("$[0].description").value(productResponseList.get(0).getDescription()))
                .andExpect(jsonPath("$[0].price").value(productResponseList.get(0).getPrice()))
                .andExpect(jsonPath("$[0].stock").value(productResponseList.get(0).getStock()))
                .andExpect(jsonPath("$[0].category").value(productResponseList.get(0).getCategory()));
    }

    @Test
    @WithMockUser(username = "admin", roles = {ADMIN, SELLER})
    @DisplayName("Should update product given a valid ID and request")
    void update_ShouldReturnUpdatedProduct_WhenValidRequestAndId() throws Exception {
        String uri = RESOURCE_PATH + "/" + PRODUCT_ID;
        ProductRequest request = DummyData.createProductRequestUpdate();
        ProductResponse updatedResponse = DummyData.createUpdatedProductResponse();

        when(productService.update(eq(request), eq(PRODUCT_ID))).thenReturn(updatedResponse);

        mockMvc.perform(put(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(updatedResponse.getId()))
                .andExpect(jsonPath("$.name").value(updatedResponse.getName()))
                .andExpect(jsonPath("$.description").value(updatedResponse.getDescription()))
                .andExpect(jsonPath("$.price").value(updatedResponse.getPrice()))
                .andExpect(jsonPath("$.stock").value(updatedResponse.getStock()))
                .andExpect(jsonPath("$.category").value(updatedResponse.getCategory()));
    }


    @Test
    @WithMockUser(username = "seller", roles = {SELLER})
    @DisplayName("Should update a product stock given a valid product ID and quantity stock")
    void updateStock_ShouldReturnUpdatedProduct_WhenValidIdAndStock() throws Exception {
        String uri = RESOURCE_PATH + "/" + PRODUCT_ID + "/stock";
        Integer newStock = 30;

        ProductResponse updatedResponse = DummyData.createProductResponse();
        updatedResponse.setStock(newStock);

        when(productService.updateStock(PRODUCT_ID, newStock)).thenReturn(updatedResponse);

        mockMvc.perform(patch(uri)
                        .param("newStock", newStock.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(updatedResponse.getId()))
                .andExpect(jsonPath("$.stock").value(newStock));
    }

    @Test
    @WithMockUser(username = "seller", roles = {SELLER})
    @DisplayName("Should delete product when ID exists")
    void delete_ShouldReturnNoContent_WhenProductExists() throws Exception {
        String uri = RESOURCE_PATH + "/" + PRODUCT_ID;
        mockMvc.perform(delete(uri))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).delete(eq(PRODUCT_ID));
    }

}