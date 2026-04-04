package com.guvi.inventory_order_mgt.exception;

// Used when: fetching a product, category, order, or user by ID that doesn't exist.
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
