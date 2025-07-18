package com.alex.ecom_cart.domain.repositories;

import com.alex.ecom_cart.domain.entities.CategoryEntity;
import org.springframework.data.repository.CrudRepository;

public interface CategoryRepository extends CrudRepository<CategoryEntity, Long> {
}
