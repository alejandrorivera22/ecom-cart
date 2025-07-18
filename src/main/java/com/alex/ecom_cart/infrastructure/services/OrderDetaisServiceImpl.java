package com.alex.ecom_cart.infrastructure.services;

import com.alex.ecom_cart.api.dtos.response.OrderDetailResponse;
import com.alex.ecom_cart.config.RedisConfig;
import com.alex.ecom_cart.domain.entities.OrderDetailEntity;
import com.alex.ecom_cart.domain.repositories.OrderDetailRepository;
import com.alex.ecom_cart.domain.repositories.OrderRepository;
import com.alex.ecom_cart.domain.repositories.ProductRepository;
import com.alex.ecom_cart.infrastructure.abstract_services.IOrderDetais;
import com.alex.ecom_cart.util.enums.Tables;
import com.alex.ecom_cart.util.exceptions.IdNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderDetaisServiceImpl implements IOrderDetais {

    private final OrderDetailRepository orderDetailRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final static short PAGE_SIZE = 5;

    @Cacheable(cacheNames = RedisConfig.CacheConstants.ORDERS_DETAILS_CACHE_NAME)
    @Override
    public Page<OrderDetailResponse> readAll(String field, Boolean desc, Integer page) {
        Sort sorting = Sort.by("product");
        if (Objects.nonNull(field)) {
            switch (field) {
                case "product" -> sorting = Sort.by("product");
                case "order" -> sorting = Sort.by("order");
                case "price" -> sorting = Sort.by("price");

                default -> throw new IllegalArgumentException("invalid field: " + field);
            }
        }
        Page<OrderDetailEntity> orderPage = desc
                ? this.orderDetailRepository.findAll(PageRequest.of(page, PAGE_SIZE, sorting.descending()))
                : this.orderDetailRepository.findAll(PageRequest.of(page, PAGE_SIZE, sorting.ascending()));

        return orderPage.map(this::entityToResponse);
    }

    @Cacheable(cacheNames = RedisConfig.CacheConstants.ORDERS_DETAILS_CACHE_NAME)
    @Override
    public List<OrderDetailResponse> findByOrderId(Long orderId) {
        if (!this.orderRepository.existsById(orderId)){
            throw new IdNotFoundException(Tables.order.name());
        }
        List<OrderDetailEntity> orderDetailsFromDb = orderDetailRepository.findByOrderId(orderId);
        return orderDetailsFromDb.stream().map(this::entityToResponse).toList();
    }

    @Cacheable(cacheNames = RedisConfig.CacheConstants.ORDERS_DETAILS_CACHE_NAME)
    @Override
    public List<OrderDetailResponse> findByProductId(Long productId) {
        if (!this.productRepository.existsById(productId)){
            throw new IdNotFoundException(Tables.product.name());
        }
        List<OrderDetailEntity> orderDetailsFromDb = orderDetailRepository.findByProductId(productId);
        return orderDetailsFromDb.stream().map(this::entityToResponse).toList();
    }

    private OrderDetailResponse entityToResponse(OrderDetailEntity orderDetail){
        OrderDetailResponse response = new OrderDetailResponse();
        BeanUtils.copyProperties(orderDetail, response);
        response.setOrder(orderDetail.getOrder().getId());
        response.setProduct(orderDetail.getProduct().getId());
        return response;
    }

}
