package com.alex.ecom_cart.domain.repositories;

import com.alex.ecom_cart.domain.entities.OrderDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface OrderDetailRepository extends JpaRepository<OrderDetailEntity, Long> {
    List<OrderDetailEntity> findByOrderId(Long orderId);
    List<OrderDetailEntity> findByProductId(Long productId);
}
