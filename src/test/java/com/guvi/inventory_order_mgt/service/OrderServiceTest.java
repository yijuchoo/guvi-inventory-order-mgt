package com.guvi.inventory_order_mgt.service;

import com.guvi.inventory_order_mgt.dto.OrderItemRequest;
import com.guvi.inventory_order_mgt.dto.OrderRequest;
import com.guvi.inventory_order_mgt.dto.OrderResponse;
import com.guvi.inventory_order_mgt.enums.OrderStatus;
import com.guvi.inventory_order_mgt.enums.ProductStatus;
import com.guvi.inventory_order_mgt.enums.Role;
import com.guvi.inventory_order_mgt.exception.InsufficientStockException;
import com.guvi.inventory_order_mgt.exception.OrderStatusException;
import com.guvi.inventory_order_mgt.exception.ResourceNotFoundException;
import com.guvi.inventory_order_mgt.exception.UnauthorizedAccessException;
import com.guvi.inventory_order_mgt.model.Order;
import com.guvi.inventory_order_mgt.model.OrderItem;
import com.guvi.inventory_order_mgt.model.Product;
import com.guvi.inventory_order_mgt.model.User;
import com.guvi.inventory_order_mgt.repo.OrderRepository;
import com.guvi.inventory_order_mgt.repo.ProductRepository;
import com.guvi.inventory_order_mgt.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    private User mockUser;
    private Product mockProduct;
    private Order mockOrder;
    private OrderRequest orderRequest;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("John Doe");
        mockUser.setEmail("john@example.com");
        mockUser.setRole(Role.USER);

        mockProduct = new Product();
        mockProduct.setId(1L);
        mockProduct.setName("Wireless Headphones");
        mockProduct.setPrice(99.99);
        mockProduct.setStockQuantity(50);
        mockProduct.setStatus(ProductStatus.ACTIVE);
        mockProduct.setCategories(new HashSet<>());

        OrderItem mockOrderItem = new OrderItem();
        mockOrderItem.setId(1L);
        mockOrderItem.setProduct(mockProduct);
        mockOrderItem.setQuantity(2);
        mockOrderItem.setUnitPrice(99.99);

        mockOrder = new Order();
        mockOrder.setId(1L);
        mockOrder.setUser(mockUser);
        mockOrder.setStatus(OrderStatus.CONFIRMED);
        mockOrder.setCreatedAt(LocalDateTime.now());
        mockOrder.setOrderItems(new ArrayList<>(List.of(mockOrderItem)));
        mockOrderItem.setOrder(mockOrder);

        OrderItemRequest itemRequest = new OrderItemRequest(1L, 2);
        orderRequest = new OrderRequest(List.of(itemRequest));

        pageable = PageRequest.of(0, 10);
    }

    // ─── Place Order Tests ────

    @Test
    void placeOrder_ShouldReturnOrderResponse_WhenStockIsSufficient() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        OrderResponse response = orderService.placeOrder(1L, orderRequest);

        assertNotNull(response);
        assertEquals(OrderStatus.CONFIRMED, response.getStatus());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void placeOrder_ShouldReduceStock_WhenOrderPlaced() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        orderService.placeOrder(1L, orderRequest);

        // Stock should be reduced by 2
        assertEquals(48, mockProduct.getStockQuantity());
    }

    @Test
    void placeOrder_ShouldThrowInsufficientStockException_WhenStockIsLow() {
        mockProduct.setStockQuantity(1); // Only 1 in stock, requesting 2

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));

        assertThrows(InsufficientStockException.class,
                () -> orderService.placeOrder(1L, orderRequest));

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void placeOrder_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.placeOrder(99L, orderRequest));
    }

    @Test
    void placeOrder_ShouldThrowResourceNotFoundException_WhenProductNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.placeOrder(1L, orderRequest));
    }

    @Test
    void placeOrder_ShouldThrowOrderStatusException_WhenProductIsInactive() {
        mockProduct.setStatus(ProductStatus.INACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));

        assertThrows(OrderStatusException.class,
                () -> orderService.placeOrder(1L, orderRequest));
    }

    // ─── Cancel Order Tests ───

    @Test
    void cancelOrder_ShouldCancelSuccessfully_WhenOrderBelongsToUser() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        OrderResponse response = orderService.cancelOrder(1L, 1L);

        assertNotNull(response);
        assertEquals(OrderStatus.CANCELLED, response.getStatus());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void cancelOrder_ShouldRestoreStock_WhenOrderCancelled() {
        mockProduct.setStockQuantity(48); // After order was placed

        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        orderService.cancelOrder(1L, 1L);

        // Stock should be restored by 2
        assertEquals(50, mockProduct.getStockQuantity());
    }

    @Test
    void cancelOrder_ShouldThrowUnauthorizedAccessException_WhenOrderNotOwnedByUser() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));

        // User ID 2 trying to cancel User 1's order
        assertThrows(UnauthorizedAccessException.class,
                () -> orderService.cancelOrder(2L, 1L));
    }

    @Test
    void cancelOrder_ShouldThrowOrderStatusException_WhenOrderAlreadyCancelled() {
        mockOrder.setStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));

        assertThrows(OrderStatusException.class,
                () -> orderService.cancelOrder(1L, 1L));
    }

    @Test
    void cancelOrder_ShouldThrowResourceNotFoundException_WhenOrderNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.cancelOrder(1L, 99L));
    }

    // ─── Get Order Tests ───

    @Test
    void getMyOrders_ShouldReturnPaginatedOrders_ForUser() {
        Page<Order> orderPage = new PageImpl<>(List.of(mockOrder));
        when(orderRepository.findByUserId(1L, pageable)).thenReturn(orderPage);

        Page<OrderResponse> responses = orderService.getMyOrders(1L, pageable);

        assertEquals(1, responses.getTotalElements());
        assertEquals(1L, responses.getContent().get(0).getUserId());
    }

    @Test
    void getOrderById_ShouldReturnOrder_WhenBelongsToUser() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));

        OrderResponse response = orderService.getOrderById(1L, 1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void getOrderById_ShouldThrowUnauthorizedAccessException_WhenOrderNotOwnedByUser() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));

        assertThrows(UnauthorizedAccessException.class,
                () -> orderService.getOrderById(2L, 1L));
    }

    @Test
    void getOrderById_ShouldThrowResourceNotFoundException_WhenOrderNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.getOrderById(1L, 99L));
    }
}