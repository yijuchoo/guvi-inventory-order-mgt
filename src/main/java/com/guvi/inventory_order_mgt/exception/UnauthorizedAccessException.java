package com.guvi.inventory_order_mgt.exception;

// Used when: a USER tries to access or cancel another user's order.
public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
