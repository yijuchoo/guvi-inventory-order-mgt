package com.guvi.inventory_order_mgt.repo;

import com.guvi.inventory_order_mgt.enums.OrderStatus;
import com.guvi.inventory_order_mgt.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Get paginated order history for a specific user
    Page<Order> findByUserId(Long userId, Pageable pageable);

    //Get orders by user and status
    Page<Order> findByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);
}
