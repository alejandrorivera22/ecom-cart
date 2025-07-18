package com.alex.ecom_cart.infrastructure.services;

import com.alex.ecom_cart.DummyData;
import com.alex.ecom_cart.api.dtos.request.ProductRequest;
import com.alex.ecom_cart.api.dtos.response.ProductResponse;
import com.alex.ecom_cart.domain.entities.CategoryEntity;
import com.alex.ecom_cart.domain.entities.ProductEntity;
import com.alex.ecom_cart.domain.repositories.CategoryRepository;
import com.alex.ecom_cart.domain.repositories.ProductRepository;
import com.alex.ecom_cart.infrastructure.cache.CacheHelper;
import com.alex.ecom_cart.util.exceptions.IdNotFoundException;
import com.alex.ecom_cart.util.exceptions.ResourceNotEnabledException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductServiceImplTest extends ServiceSpec{

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CacheHelper cacheHelper;

    @InjectMocks
    private ProductServiceImpl productService;

    private CategoryEntity category;
    private ProductEntity product;
    private long productId;

    @BeforeEach
    void setUp() {
        category = DummyData.createCategoryEntity();
        product = DummyData.createProductEntity(category);
        productId = product.getId();
    }

    @Test
    void readAll() {

        List<ProductEntity> productList = List.of(product);

        Pageable pageable = PageRequest.of(0, 5, Sort.by("name").ascending());
        Page<ProductEntity> productPage = new PageImpl<>(productList, pageable, productList.size());

        when(productRepository.findAllActive(any(PageRequest.class))).thenReturn(productPage);

        Page<ProductResponse> resultPage = productService.readAll(null, false, 0);

        assertNotNull(resultPage);
        assertEquals(1, resultPage.getTotalElements());
        assertEquals(product.getName(), resultPage.getContent().get(0).getName());

    }

    @Test
    @DisplayName("should create a product and return a response")
    void create_ShouldSaveProductAndReturnResponse() {

        ProductRequest request = DummyData.createProductRequest();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(ProductEntity.class))).thenReturn(product);

        ProductResponse response = productService.create(request);

        assertNotNull(response);
        assertEquals(request.getName(), response.getName());
        verify(productRepository, times(1)).save(any(ProductEntity.class));

    }

    @Test
    @DisplayName("happy path should return product when it exists and is enabled")
    void findById_ShouldReturnProduct_WhenEnabled() {

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        ProductResponse response = productService.findById(productId);

        assertNotNull(response);
        assertAll(
                () -> assertEquals(product.getId(), response.getId()),
                () -> assertEquals(product.getName(), response.getName()),
                () -> assertEquals(product.getDescription(), response.getDescription())
        );

    }


    @Test
    @DisplayName("Unhappy path  Should throw IdNotFoundException when product does not exist")
    void findById_ShouldThrowException_WhenProductIdNotFound() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        assertThrows(IdNotFoundException.class, () -> productService.findById(productId));
    }

    @Test
    @DisplayName("Unhappy path  Should throw ResourceNotEnabledException when product is not enabled")
    void findById_ShouldThrowException_WhenProductDisabled() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(DummyData.createProductEntityDisaled(category)));
        assertThrows(ResourceNotEnabledException.class, () -> productService.findById(productId));
    }

    @Test
    @DisplayName("Should return all products given a valid category ID")
    void findByCategoryId_ShouldReturnProducts_WhenCategoryIdExists() {

        CategoryEntity category = DummyData.createCategoryEntity();
        List<ProductEntity> products = DummyData.createProductEntityList(category);
        Long categoryId = category.getId();

        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        when(productRepository.findByCategoryIdAndNotDeleted(categoryId)).thenReturn(products);
        List<ProductResponse> response = productService.findByCategoryId(categoryId);

        assertNotNull(response);

        int expectedElements = 2;
        assertEquals(expectedElements, response.size());
    }

    @Test
    @DisplayName("should return all desabled products")
    void findDisabledProducts_ShouldReturnAllDisabledProducts() {

        List<ProductEntity> products = List.of(DummyData.createProductEntityDisaled(category));

        when(productRepository.findAllByEnabledFalse()).thenReturn(products);
        List<ProductResponse> response = productService.findDisabledProducts();

        assertNotNull(response);

        int expectedElements = 1;
        assertEquals(expectedElements, response.size());

    }

    @Test
    @DisplayName("Should update a product given a valid request and product ID")
    void update_ShouldReturnUpdateProduct() {

        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ProductResponse response = productService.findById(productId);

        assertNotNull(response);
        assertAll(
                () -> assertEquals(product.getId(), response.getId()),
                () -> assertEquals(product.getName(), response.getName()),
                () -> assertEquals(product.getPrice(), response.getPrice()),
                () -> assertEquals(product.getStock(), response.getStock())
        );

        ProductRequest updateRequest = DummyData.createProductRequestUpdate();
        ProductResponse updateResponse = productService.update(updateRequest, productId);

        assertNotNull(updateResponse);
        assertAll(
                () -> assertEquals(1, updateResponse.getId()),
                () -> assertEquals(updateRequest.getName(), updateResponse.getName()),
                () -> assertEquals(updateRequest.getDescription(), updateResponse.getDescription()),
                () -> assertEquals(updateRequest.getPrice(), updateResponse.getPrice()),
                () -> assertEquals(updateRequest.getStock(), updateResponse.getStock())
        );
    }


    @Test
    @DisplayName("Unhappy path Should throw ResourceNotEnabledException when product is not enabled")
    void update_ShouldThrowException_WhenProductDisabled() {
        ProductEntity disabledProduct = DummyData.createProductEntityDisaled(category);
        ProductRequest updateRequest = DummyData.createProductRequest();

        when(productRepository.findById(productId)).thenReturn(Optional.of(disabledProduct));
        when(categoryRepository.findById(updateRequest.getCategory())).thenReturn(Optional.of(category));

        assertThrows(ResourceNotEnabledException.class, () -> productService.update(updateRequest, productId));
    }

    @Test
    @DisplayName("Should update a product stock given a valid product ID and quantity stock")
    void updateStock() {

        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productService.findById(productId);

        assertNotNull(response);
        assertAll(
                () -> assertEquals(1, response.getId()),
                () -> assertEquals(product.getName(), response.getName()),
                () -> assertEquals(product.getPrice(), response.getPrice()),
                () -> assertEquals(product.getStock(), response.getStock())
        );

        ProductResponse updateResponse = productService.updateStock(productId, 25);

        assertNotNull(updateResponse);
        assertAll(
                () -> assertEquals(1, updateResponse.getId()),
                () -> assertEquals(product.getName(), updateResponse.getName()),
                () -> assertEquals(product.getStock(), updateResponse.getStock())
        );

    }

    @Test
    @DisplayName("Should delete or didabled a product")
    void delete() {

        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ProductResponse response = productService.findById(1L);

        assertNotNull(response);
        assertTrue(product.isEnabled());
        productService.delete(productId);

        verify(productRepository, atLeastOnce()).delete(any(ProductEntity.class));
    }
}