package com.alex.ecom_cart.infrastructure.services;

import com.alex.ecom_cart.api.dtos.request.OrderProductRequest;
import com.alex.ecom_cart.api.dtos.request.OrderRequest;
import com.alex.ecom_cart.api.dtos.response.OrderResponse;
import com.alex.ecom_cart.config.RedisConfig;
import com.alex.ecom_cart.domain.entities.OrderDetailEntity;
import com.alex.ecom_cart.domain.entities.OrderEntity;
import com.alex.ecom_cart.domain.entities.ProductEntity;
import com.alex.ecom_cart.domain.entities.CustomerEntity;
import com.alex.ecom_cart.domain.repositories.OrderDetailRepository;
import com.alex.ecom_cart.domain.repositories.OrderRepository;
import com.alex.ecom_cart.domain.repositories.ProductRepository;
import com.alex.ecom_cart.domain.repositories.CustomerRepository;
import com.alex.ecom_cart.infrastructure.abstract_services.IOrderService;
import com.alex.ecom_cart.util.enums.OrderStatus;
import com.alex.ecom_cart.util.enums.Tables;
import com.alex.ecom_cart.util.exceptions.IdNotFoundException;
import com.alex.ecom_cart.util.exceptions.InsufficientStockException;
import com.alex.ecom_cart.util.exceptions.ResourceNotEnabledException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final static short PAGE_SIZE = 5;

    @Cacheable(value = RedisConfig.CacheConstants.ORDERS_CACHE_NAME)
    @Override
    public Page<OrderResponse> readAll(String field, Boolean desc, Integer page) {
        Sort sorting = Sort.by("customer");
        if (Objects.nonNull(field)) {
            switch (field) {
                case "customer" -> sorting = Sort.by("customer");
                case "totalPrice" -> sorting = Sort.by("totalPrice");

                default -> throw new IllegalArgumentException("invalid field: " + field);
            }
        }
        Page<OrderEntity> orderPage = desc
                ? this.orderRepository.findAll(PageRequest.of(page, PAGE_SIZE, sorting.descending()))
                : this.orderRepository.findAll(PageRequest.of(page, PAGE_SIZE, sorting.ascending()));

        return orderPage.map(this::entityToResponse);
    }

    @Override
    public OrderResponse create(OrderRequest request) {
        CustomerEntity customerFromDb = validateAndGetCustomer(request.getCustomerId());
        // Create the order
        OrderEntity order = createOrder(customerFromDb);

        // Process the products
        BigDecimal totalPrice = processOrderProducts(request, order);

        //Set the total price of the order
        order.setTotalPrice(totalPrice);

        //Save the order
        OrderEntity orderPersisted = orderRepository.save(order);

        //save the details
        for (OrderDetailEntity detail : order.getOrderDetails()) {
            detail.setOrder(orderPersisted);
            orderDetailRepository.save(detail);
        }

        return entityToResponse(orderPersisted);

    }


    private CustomerEntity validateAndGetCustomer(Long customerId) {
        CustomerEntity customerFromDb = customerRepository.findById(customerId)
                .orElseThrow(() -> new IdNotFoundException(Tables.customer.name()));

        if (!customerFromDb.isEnabled()) {
            throw new ResourceNotEnabledException(customerFromDb.getUsername());
        }

        return customerFromDb;
    }


    private OrderEntity createOrder(CustomerEntity customer) {
        return OrderEntity.builder()
                .customer(customer)
                .orderStatus(OrderStatus.PENDING)
                .totalPrice(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();
    }


    private BigDecimal processOrderProducts(OrderRequest request, OrderEntity order) {
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (OrderProductRequest p : request.getProducts()) {
            ProductEntity product = validateAndGetProduct(p.getProductId(), p.getQuantity());

            // Reduce product stock
            product.setStock(product.getStock() - p.getQuantity());
            productRepository.save(product);

            //Create order detail
            createOrderDetail(order, product, p.getQuantity());

            //Calculate the total price
            totalPrice = totalPrice.add(product.getPrice().multiply(BigDecimal.valueOf(p.getQuantity())));
        }

        return totalPrice;
    }

    private ProductEntity validateAndGetProduct(Long productId, int quantity) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new IdNotFoundException(Tables.product.name()));

        if (!product.isEnabled()) {
            throw new ResourceNotEnabledException(product.getName());
        }

        if (product.getStock() < quantity) {
            throw new InsufficientStockException(product.getName());
        }

        return product;
    }

    private void createOrderDetail(OrderEntity order, ProductEntity product, int quantity) {
        OrderDetailEntity detail = OrderDetailEntity.builder()
                .product(product)
                .quantity(quantity)
                .price(product.getPrice())
                .order(order)
                .createdAt(LocalDateTime.now())
                .build();

        order.getOrderDetails().add(detail);
    }

    @Cacheable(value = RedisConfig.CacheConstants.ORDERS_CACHE_NAME)
    @Override
    public OrderResponse findById(Long id) {
        OrderEntity orderFromDb = this.orderRepository.findById(id)
                .orElseThrow(() -> new IdNotFoundException(Tables.order.name()));
        return entityToResponse(orderFromDb);
    }

    @Cacheable(value = RedisConfig.CacheConstants.ORDERS_CACHE_NAME)
    @Override
    public List<OrderResponse> findByCustomerId(Long customerId) {
        if (!customerRepository.existsById(customerId)){
            throw new IdNotFoundException(Tables.customer.name());
        }
        List<OrderEntity> orderFromDb = this.orderRepository.findByCustomerId(customerId);
        return orderFromDb
                .stream()
                .map(this::entityToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse updateStatus(OrderStatus request, Long id) {

        OrderEntity orderToUpdate = this.orderRepository.findById(id)
                .orElseThrow(() -> new IdNotFoundException(Tables.order.name()));

        OrderStatus current = orderToUpdate.getOrderStatus();

        if (request == null) {
            throw new IllegalArgumentException("The new order state cannot be null");
        }

        if (!VALID_TRANSITIONS.getOrDefault(current, Set.of()).contains(request)){
            throw new IllegalStateException(String.format(
                    "Cannot change state of %s to %s", current, request
            ));
        }

        orderToUpdate.setOrderStatus(request);

        OrderEntity orderUpdated = this.orderRepository.save(orderToUpdate);

        return entityToResponse(orderUpdated);
    }

    @Override
    public OrderResponse cancelOrder(Long id) {
        OrderEntity orderFromDb = this.orderRepository.findById(id)
                .orElseThrow(() -> new IdNotFoundException(Tables.order.name()));

        if (orderFromDb.getOrderStatus() == OrderStatus.COMPLETED || orderFromDb.getOrderStatus() == OrderStatus.SHIPPED){
            throw new IllegalStateException("You cannot cancel an order that has already been completed or shipped");
        }

        orderFromDb.setOrderStatus(OrderStatus.CANCELLED);
        this.orderRepository.save(orderFromDb);

        return entityToResponse(orderFromDb);

    }


    private OrderResponse entityToResponse(OrderEntity orderEntity) {
        OrderResponse response = new OrderResponse();
        BeanUtils.copyProperties(orderEntity, response);
        response.setUsername(orderEntity.getCustomer().getUsername());

        return response;
    }

    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = Map.of(
            OrderStatus.PENDING, Set.of(OrderStatus.SHIPPED),
            OrderStatus.SHIPPED, Set.of(OrderStatus.COMPLETED),
            OrderStatus.COMPLETED, Set.of(),
            OrderStatus.CANCELLED, Set.of()
    );


}
