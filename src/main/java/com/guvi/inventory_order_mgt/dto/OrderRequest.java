package com.guvi.inventory_order_mgt.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class OrderRequest {
    // Purpose: Place order with list of items

    @NotNull(message = "Order items are required")
    @NotEmpty(message = "Order must contain at least one item")
    private List<OrderItemRequest> items;

    // Constructors
    public OrderRequest() {
    }

    public OrderRequest(List<OrderItemRequest> items) {
        this.items = items;
    }

    // Getters & Setters
    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }
}
