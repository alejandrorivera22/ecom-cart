package com.alex.ecom_cart.infrastructure.services;

import com.alex.ecom_cart.DummyData;
import com.alex.ecom_cart.api.dtos.request.OrderProductRequest;
import com.alex.ecom_cart.api.dtos.request.OrderRequest;
import com.alex.ecom_cart.api.dtos.response.OrderResponse;
import com.alex.ecom_cart.domain.entities.*;
import com.alex.ecom_cart.domain.repositories.CustomerRepository;
import com.alex.ecom_cart.domain.repositories.OrderDetailRepository;
import com.alex.ecom_cart.domain.repositories.OrderRepository;
import com.alex.ecom_cart.domain.repositories.ProductRepository;
import com.alex.ecom_cart.util.enums.OrderStatus;
import com.alex.ecom_cart.util.exceptions.IdNotFoundException;
import com.alex.ecom_cart.util.exceptions.InsufficientStockException;
import com.alex.ecom_cart.util.exceptions.ResourceNotEnabledException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceImplTest extends ServiceSpec{

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private OrderDetailRepository orderDetailRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private CustomerEntity customer;
    private ProductEntity product;
    private OrderRequest orderRequest;

    @BeforeEach
    void setUp() {
        customer = DummyData.createCustomerEntity(DummyData.createRoleEntityCustomer());
        product = DummyData.createProductEntity(DummyData.createCategoryEntity());

        OrderProductRequest productRequest = OrderProductRequest.builder()
                .productId(product.getId())
                .quantity(2)
                .build();

        orderRequest = OrderRequest.builder()
                .customerId(customer.getId())
                .products(List.of(productRequest))
                .build();
    }

    @Test
    void readAll() {
        OrderEntity order = DummyData.createOrderEntity(customer, List.of());
        order.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(2)));

        List<OrderEntity> orders = List.of(order);
        Pageable pageable = PageRequest.of(0, 5, Sort.by("totalPrice").ascending());
        Page<OrderEntity> orderPage = new PageImpl<>(orders, pageable, orders.size());

        when(orderRepository.findAll(any(PageRequest.class))).thenReturn(orderPage);

        Page<OrderResponse> result = orderService.readAll("totalPrice", false, 0);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(order.getTotalPrice(), result.getContent().get(0).getTotalPrice());
        assertEquals(order.getCustomer().getUsername(), result.getContent().get(0).getUsername());

        verify(orderRepository).findAll(any(PageRequest.class));
    }

    @Test
    @DisplayName("Happy path Should create an order with valid customer and products")
    void create_ShouldReturnOrderResponse() {
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> {
            OrderEntity order = inv.getArgument(0);
            order.setId(1L);
            return order;
        });
        when(orderDetailRepository.save(any(OrderDetailEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.create(orderRequest);

        assertNotNull(response);
        assertEquals(customer.getUsername(), response.getUsername());
        verify(orderRepository).save(any(OrderEntity.class));
        verify(orderDetailRepository, times(1)).save(any(OrderDetailEntity.class));
    }

    @Test
    @DisplayName("Unhappy path Should throw IdNotFoundException when customer is not found")
    void create_ShouldThrow_WhenCustomerNotFound() {
        when(customerRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(IdNotFoundException.class, () -> orderService.create(orderRequest));
    }

    @Test
    @DisplayName("Unhappy path Should throw ResourceNotEnabledException when customer is disabled")
    void createOrder_ShouldThrow_WhenCustomerDisabled() {
        customer.setEnabled(false);
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));

        assertThrows(ResourceNotEnabledException.class, () -> orderService.create(orderRequest));
    }

    @Test
    @DisplayName("Unhappy path Should throw ResourceNotEnabledException when product is disabled")
    void create_ShouldThrow_WhenProductDisabled() {
        product.setEnabled(false);
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

        assertThrows(ResourceNotEnabledException.class, () -> orderService.create(orderRequest));
    }

    @Test
    @DisplayName("Unhappy path Should throw InsufficientStockException when product stock is insufficient")
    void create_ShouldThrow_WhenInsufficientStock() {
        product.setStock(1);
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

        assertThrows(InsufficientStockException.class, () -> orderService.create(orderRequest));
    }

    @Test
    @DisplayName("Happy path Should return an order when ID is valid")
    void findById_ShouldReturnOrderResponse_WhenIdExists() {
        CustomerEntity customer = DummyData.createCustomerEntity(DummyData.createRoleEntityCustomer());
        ProductEntity product = DummyData.createProductEntity(DummyData.createCategoryEntity());

        OrderDetailEntity detail = DummyData.createOrderDetailEntity(null, product);
        OrderEntity order = DummyData.createOrderEntity(customer, List.of(detail));
        detail.setOrder(order); // setear relación bidireccional

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        OrderResponse response = orderService.findById(order.getId());

        assertNotNull(response);
        assertEquals(order.getId(), response.getId());
        assertEquals(order.getCustomer().getUsername(), response.getUsername());
        assertEquals(order.getTotalPrice(), response.getTotalPrice());
        assertEquals(order.getOrderStatus(), response.getOrderStatus());
    }

    @Test
    @DisplayName("Unhappy pathShould throw IdNotFoundException when order ID does not exist")
    void findById_ShouldThrowException_WhenIdNotFound() {
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(IdNotFoundException.class, () -> orderService.findById(orderId));
    }

    @Test
    @DisplayName("Should return order list for given customer ID")
    void findByCustomerId_ShouldReturnOrders() {
        CustomerEntity customer = DummyData.createCustomerEntity(DummyData.createRoleEntityCustomer());
        ProductEntity product = DummyData.createProductEntity(DummyData.createCategoryEntity());

        OrderDetailEntity detail = DummyData.createOrderDetailEntity(null, product);
        OrderEntity order = DummyData.createOrderEntity(customer, List.of(detail));
        detail.setOrder(order); // Relación bidireccional

        when(customerRepository.existsById(customer.getId())).thenReturn(true);
        when(orderRepository.findByCustomerId(customer.getId())).thenReturn(List.of(order));

        List<OrderResponse> result = orderService.findByCustomerId(customer.getId());

        assertEquals(1, result.size());
        assertEquals(order.getId(), result.get(0).getId());
        assertEquals(customer.getUsername(), result.get(0).getUsername());
    }

    @Test
    @DisplayName("Should update order status from PENDING to SHIPPED")
    void updateStatus_ShouldChangeStatus_WhenTransitionIsValid() {
        CustomerEntity customer = DummyData.createCustomerEntity(DummyData.createRoleEntityCustomer());
        ProductEntity product = DummyData.createProductEntity(DummyData.createCategoryEntity());

        OrderDetailEntity detail = DummyData.createOrderDetailEntity(null, product);
        OrderEntity order = DummyData.createOrderEntity(customer, List.of(detail));
        detail.setOrder(order); // vincular relación

        order.setOrderStatus(OrderStatus.PENDING);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.updateStatus(OrderStatus.SHIPPED, order.getId());

        assertNotNull(response);
        assertEquals(OrderStatus.SHIPPED, response.getOrderStatus());
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("Should throw exception for invalid status transition")
    void updateStatus_ShouldThrow_WhenTransitionIsInvalid() {
        OrderEntity order = DummyData.createOrderEntity(
                DummyData.createCustomerEntity(DummyData.createRoleEntityCustomer()),
                List.of()
        );
        order.setOrderStatus(OrderStatus.PENDING);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () ->
                orderService.updateStatus(OrderStatus.COMPLETED, order.getId())
        );
    }

    @Test
    @DisplayName("Should cancel an order when status is PENDING")
    void cancel_ShouldCancelledOrder_WhenPending() {
        CustomerEntity customer = DummyData.createCustomerEntity(DummyData.createRoleEntityCustomer());
        OrderEntity order = DummyData.createOrderEntity(customer, List.of());
        order.setOrderStatus(OrderStatus.PENDING);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.cancelOrder(order.getId());

        assertNotNull(response);
        assertEquals(OrderStatus.CANCELLED, response.getOrderStatus());
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("Should throw exception if order is already COMPLETED or SHIPPED")
    void cancelOrder_ShouldThrowException_WhenStatusIsInvalid() {
        CustomerEntity customer = DummyData.createCustomerEntity(DummyData.createRoleEntityCustomer());
        OrderEntity order = DummyData.createOrderEntity(customer, List.of());

        order.setOrderStatus(OrderStatus.COMPLETED);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> orderService.cancelOrder(order.getId()));
    }
}