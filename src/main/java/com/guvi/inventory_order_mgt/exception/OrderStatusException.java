package com.guvi.inventory_order_mgt.exception;

// Used when: an invalid order status transition is attempted — e.g. trying to cancel an already CONFIRMED order, or
// confirming a CANCELLED order.
public class OrderStatusException extends RuntimeException {

    public OrderStatusException(String message) {
        super(message);
    }
}
