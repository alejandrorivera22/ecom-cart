package com.alex.ecom_cart.infrastructure.abstract_services;

import com.alex.ecom_cart.api.dtos.request.ProductRequest;
import com.alex.ecom_cart.api.dtos.response.ProductResponse;

import java.util.List;

public interface IProductService extends CrudPaginationService<ProductRequest, ProductResponse, Long>{
    List<ProductResponse> findByCategoryId(Long categoryId);
    List<ProductResponse> findDisabledProducts();
    ProductResponse updateStock(Long id, Integer newStock);
}
