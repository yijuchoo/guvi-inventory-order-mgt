package com.guvi.inventory_order_mgt.dto;

import com.guvi.inventory_order_mgt.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {
    // Purpose: Return order with items + total

    private Long id;
    private Long userId;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
    private double totalAmount;

    // Constructors
    public OrderResponse() {
    }

    public OrderResponse(Long id, Long userId, OrderStatus status, LocalDateTime createdAt,
                         List<OrderItemResponse> items) {
        this.id = id;
        this.userId = userId;
        this.status = status;
        this.createdAt = createdAt;
        this.items = items;
        this.totalAmount = items.stream()
                .mapToDouble(OrderItemResponse::getSubtotal)
                .sum();
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }

    public void setItems(List<OrderItemResponse> items) {
        this.items = items;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
}
