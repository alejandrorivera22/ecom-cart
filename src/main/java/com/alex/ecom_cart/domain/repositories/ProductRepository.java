package com.alex.ecom_cart.domain.repositories;

import com.alex.ecom_cart.api.dtos.response.ProductResponse;
import com.alex.ecom_cart.domain.entities.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    @Query("SELECT p FROM product p WHERE p.enabled  = true")
    Page<ProductEntity> findAllActive(PageRequest pageRequest);

    @Query("SELECT p FROM product p WHERE p.category.id = :categoryId AND p.enabled  = true")
    List<ProductEntity> findByCategoryIdAndNotDeleted(Long categoryId);

    @Query("SELECT p FROM product p WHERE p.enabled = false")
    List<ProductEntity> findAllByEnabledFalse();
}

