package com.guvi.inventory_order_mgt.exception;

// Used when: a user tries to place an order but the requested quantity exceeds available stock.
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }
}
