package com.guvi.inventory_order_mgt.service;

import com.guvi.inventory_order_mgt.dto.OrderItemRequest;
import com.guvi.inventory_order_mgt.dto.OrderItemResponse;
import com.guvi.inventory_order_mgt.dto.OrderRequest;
import com.guvi.inventory_order_mgt.dto.OrderResponse;
import com.guvi.inventory_order_mgt.enums.OrderStatus;
import com.guvi.inventory_order_mgt.enums.ProductStatus;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Purpose: Place order, cancel order, view order history
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrderResponse placeOrder(Long userId, OrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with ID: " + userId));

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());

        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with ID: " + itemRequest.getProductId()));

            // Check product is active
            if (product.getStatus() != ProductStatus.ACTIVE) {
                throw new OrderStatusException(
                        "Product is not available: " + product.getName());
            }

            // Check sufficient stock
            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product: " + product.getName() +
                                ". Available: " + product.getStockQuantity() +
                                ", Requested: " + itemRequest.getQuantity());
            }

            // Deduct stock
            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
            productRepository.save(product);

            // Build order item — snapshot unit price at time of order
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            orderItems.add(orderItem);
        }

        order.setOrderItems(orderItems);
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        return toResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with ID: " + orderId));

        // Ensure the order belongs to the requesting user
        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException(
                    "You are not authorized to cancel this order");
        }

        // Only CREATED or CONFIRMED orders can be cancelled
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new OrderStatusException("Order is already cancelled");
        }

        // Restore stock for each item
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        return toResponse(order);
    }

    public Page<OrderResponse> getMyOrders(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable)
                .map(this::toResponse);
    }

    public OrderResponse getOrderById(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with ID: " + orderId));

        // Ensure the order belongs to the requesting user
        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException(
                    "You are not authorized to view this order");
        }

        return toResponse(order);
    }

    // Mapper — Order entity to OrderResponse DTO
    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getOrderItems()
                .stream()
                .map(item -> new OrderItemResponse(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice()
                ))
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getStatus(),
                order.getCreatedAt(),
                itemResponses
        );
    }
}
