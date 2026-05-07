package com.guvi.inventory_order_mgt.model;

import com.guvi.inventory_order_mgt.enums.OrderStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One user have many orders
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    /*
    One Order can have MANY OrderItems
    * Order #1:
    * Item 1 → iPhone x2
    * Item 2 → AirPods x1
    * mappedBy = "order" -> The relationship is controlled by the order field inside OrderItem
    * OrderItem owns the relationship; Order is just referencing it
    * cascade = CascadeType.ALL -> When I perform operations on Order, apply them to OrderItems too
    * Includes:
        PERSIST (save)
        MERGE (update)
        REMOVE (delete) etc.
        eg. Order order = new Order();
            order.getOrderItems().add(item1);
            orderRepository.save(order);
    * orphanRemoval = true -> If an OrderItem is removed from the list, delete it from DB
    */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    // Constructor
    public Order() {
    }

    public Order(Long id, User user, OrderStatus status, LocalDateTime createdAt, List<OrderItem> orderItems) {
        this.id = id;
        this.user = user;
        this.status = status;
        this.createdAt = createdAt;
        this.orderItems = orderItems;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }
}
