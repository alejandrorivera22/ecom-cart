package com.alex.ecom_cart.infrastructure.services;

import com.alex.ecom_cart.api.dtos.request.ProductRequest;
import com.alex.ecom_cart.api.dtos.response.ProductResponse;
import com.alex.ecom_cart.config.RedisConfig;
import com.alex.ecom_cart.domain.entities.CategoryEntity;
import com.alex.ecom_cart.domain.entities.ProductEntity;
import com.alex.ecom_cart.domain.repositories.CategoryRepository;
import com.alex.ecom_cart.domain.repositories.ProductRepository;
import com.alex.ecom_cart.infrastructure.abstract_services.IProductService;
import com.alex.ecom_cart.infrastructure.cache.CacheHelper;
import com.alex.ecom_cart.util.enums.Tables;
import com.alex.ecom_cart.util.exceptions.IdNotFoundException;
import com.alex.ecom_cart.util.exceptions.ResourceNotEnabledException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements IProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private static final short PAGE_SIZE = 5;
    private final CacheHelper cacheHelper;

    @Override
    public Page<ProductResponse> readAll(String field, Boolean desc, Integer page) {

        Sort sorting = Sort.by("name");
        if (Objects.nonNull(field)){
            switch (field){
                case "name" -> sorting = Sort.by("name");
                case "description" -> sorting = Sort.by("description");
                case "price" -> sorting = Sort.by("price");
                default -> throw new IllegalArgumentException("Invalid field: " + field);
            }
        }
        Page<ProductEntity> products = desc
                ? this.productRepository.findAllActive(PageRequest.of(page, PAGE_SIZE, sorting.descending()))
                : this.productRepository.findAllActive(PageRequest.of(page, PAGE_SIZE, sorting.ascending()));
        return products.map(this::entityToResponse);
    }

    @CacheEvict(cacheNames = RedisConfig.CacheConstants.PRODUCT_CACHE_NAME, allEntries = true)
    @Override
    public ProductResponse create(ProductRequest request) {
        CategoryEntity categoryFromDb = this.categoryRepository.findById(request.getCategory())
                .orElseThrow(() -> new IdNotFoundException(Tables.category.name()));
        ProductEntity productToPersist = ProductEntity
                .builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .createdAt(LocalDateTime.now())
                .category(categoryFromDb)
                .enabled(true)
                .build();

        ProductEntity productPersisted = this.productRepository.save(productToPersist);

        return this.entityToResponse(productPersisted);
    }

    @Cacheable(value = RedisConfig.CacheConstants.PRODUCT_CACHE_NAME, key = "'product:id:' + #id")
    @Override
    public ProductResponse findById(Long id) {

        ProductEntity productFromDb = this.productRepository.findById(id)
                .orElseThrow(() -> new IdNotFoundException(Tables.product.name()));

        if (!productFromDb.isEnabled()){
            throw new ResourceNotEnabledException(Tables.product.name());
        }

        return entityToResponse(productFromDb);
    }

    @Cacheable(value = RedisConfig.CacheConstants.PRODUCT_CACHE_NAME, key = "'product:categoryId:' + #id")
    @Override
    public List<ProductResponse> findByCategoryId(Long id) {
        if (!this.categoryRepository.existsById(id)){
            throw  new IdNotFoundException(Tables.category.name());
        }
        List<ProductEntity> products = this.productRepository.findByCategoryIdAndNotDeleted(id);

        return products.stream()
                .map(this::entityToResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = RedisConfig.CacheConstants.PRODUCT_CACHE_NAME)
    @Override
    public List<ProductResponse> findDisabledProducts() {
        List<ProductEntity> products = this.productRepository.findAllByEnabledFalse();
        return products.stream()
                .map(this::entityToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponse update(ProductRequest request, Long id) {

        CategoryEntity categoryFromDb = this.categoryRepository.findById(request.getCategory())
                .orElseThrow(() -> new IdNotFoundException(Tables.category.name()));

        ProductEntity productToUpdate = this.productRepository.findById(id)
                .orElseThrow(() -> new IdNotFoundException(Tables.product.name()));

        if (!productToUpdate.isEnabled()){
            throw new ResourceNotEnabledException(Tables.product.name());
        }

        evictProductCache(productToUpdate);

        productToUpdate.setName(request.getName());
        productToUpdate.setDescription(request.getDescription());
        productToUpdate.setPrice(request.getPrice());
        productToUpdate.setStock(request.getStock());
        productToUpdate.setCategory(categoryFromDb);

        ProductEntity productUpdated = this.productRepository.save(productToUpdate);
        ProductResponse response = entityToResponse(productUpdated);

        putProductCache(productToUpdate, response);

        return response;
    }

    @Override
    public ProductResponse updateStock(Long id, Integer newStock) {
        ProductEntity productFromDb = this.productRepository.findById(id)
                .orElseThrow(() -> new IdNotFoundException(Tables.product.name()));

        if (!productFromDb.isEnabled()) {
            throw new ResourceNotEnabledException(Tables.product.name());
        }

        evictProductCache(productFromDb);

        productFromDb.setStock(newStock);
        ProductEntity updatedProduct = this.productRepository.save(productFromDb);

        ProductResponse response = entityToResponse(updatedProduct);

        putProductCache(productFromDb, response);

        return response;
    }

    @CacheEvict(cacheNames = RedisConfig.CacheConstants.PRODUCT_CACHE_NAME, allEntries = true)
    @Override
    public void delete(Long id) {

        ProductEntity productToDelete = this.productRepository.findById(id)
                .orElseThrow(() -> new IdNotFoundException(Tables.product.name()));

        if (!productToDelete.getOrderDetails().isEmpty() || !productToDelete.getCartProducts().isEmpty()) {
            productToDelete.disable();
            this.productRepository.save(productToDelete);
            log.info("Product with ID {} is associated with orders or carts. The product has been disabled instead of deleted.", productToDelete.getId());
        } else {
            productToDelete.setCategory(null);

            this.productRepository.delete(productToDelete);
        }

    }

    private ProductResponse entityToResponse(ProductEntity productEntity) {

        ProductResponse response = new ProductResponse();
        BeanUtils.copyProperties(productEntity, response);
        response.setCategory(productEntity.getCategory().getId());

        return response;
    }

    private void putProductCache(ProductEntity product, ProductResponse response){
        String cacheName = RedisConfig.CacheConstants.PRODUCT_CACHE_NAME;
        cacheHelper.putCacheValues(cacheName, response,
                "product:id:" + product.getId());
    }

    private void evictProductCache(ProductEntity product){
        String cacheName = RedisConfig.CacheConstants.PRODUCT_CACHE_NAME;
        cacheHelper.evictCacheKeys(cacheName, "product:id:" + product.getId(),
                "product:categoryId:" + product.getCategory().getId());
    }


}
