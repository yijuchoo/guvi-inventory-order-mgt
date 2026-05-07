package com.guvi.inventory_order_mgt.repo;

import com.guvi.inventory_order_mgt.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Fetch all items that belong to a specific order
    List<OrderItem> findByOrderId(Long orderId);
}
