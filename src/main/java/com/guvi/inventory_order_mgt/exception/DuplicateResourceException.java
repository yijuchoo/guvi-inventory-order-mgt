package com.guvi.inventory_order_mgt.exception;

// Used when: registering with an email that already exists, or creating a category with a name that already exists.
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }

}
