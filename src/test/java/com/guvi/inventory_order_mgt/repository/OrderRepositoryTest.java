package com.guvi.inventory_order_mgt.repository;

import com.guvi.inventory_order_mgt.enums.OrderStatus;
import com.guvi.inventory_order_mgt.enums.Role;
import com.guvi.inventory_order_mgt.model.Order;
import com.guvi.inventory_order_mgt.model.User;
import com.guvi.inventory_order_mgt.repo.OrderRepository;
import com.guvi.inventory_order_mgt.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Order confirmedOrder;
    private Order cancelledOrder;
    private PageRequest pageable;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setPasswordHash("hashedPassword");
        testUser.setRole(Role.USER);
        userRepository.save(testUser);

        confirmedOrder = new Order();
        confirmedOrder.setUser(testUser);
        confirmedOrder.setStatus(OrderStatus.CONFIRMED);
        confirmedOrder.setCreatedAt(LocalDateTime.now());
        confirmedOrder.setOrderItems(new ArrayList<>());
        orderRepository.save(confirmedOrder);

        cancelledOrder = new Order();
        cancelledOrder.setUser(testUser);
        cancelledOrder.setStatus(OrderStatus.CANCELLED);
        cancelledOrder.setCreatedAt(LocalDateTime.now());
        cancelledOrder.setOrderItems(new ArrayList<>());
        orderRepository.save(cancelledOrder);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void findByUserId_ShouldReturnAllOrdersForUser() {
        Page<Order> result = orderRepository
                .findByUserId(testUser.getId(), pageable);

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void findByUserId_ShouldReturnEmpty_WhenUserHasNoOrders() {
        // Create another user with no orders
        User otherUser = new User();
        otherUser.setName("Jane Doe");
        otherUser.setEmail("jane@example.com");
        otherUser.setPasswordHash("hashedPassword");
        otherUser.setRole(Role.USER);
        userRepository.save(otherUser);

        Page<Order> result = orderRepository
                .findByUserId(otherUser.getId(), pageable);

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void findByUserIdAndStatus_ShouldReturnOnlyConfirmedOrders() {
        Page<Order> result = orderRepository
                .findByUserIdAndStatus(
                        testUser.getId(), OrderStatus.CONFIRMED, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(OrderStatus.CONFIRMED,
                result.getContent().get(0).getStatus());
    }

    @Test
    void findByUserIdAndStatus_ShouldReturnOnlyCancelledOrders() {
        Page<Order> result = orderRepository
                .findByUserIdAndStatus(
                        testUser.getId(), OrderStatus.CANCELLED, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(OrderStatus.CANCELLED,
                result.getContent().get(0).getStatus());
    }

    @Test
    void findByUserIdAndStatus_ShouldReturnEmpty_WhenNoMatchingStatus() {
        Page<Order> result = orderRepository
                .findByUserIdAndStatus(
                        testUser.getId(), OrderStatus.CREATED, pageable);

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void save_ShouldPersistOrder_WithGeneratedId() {
        assertNotNull(confirmedOrder.getId());
    }
}