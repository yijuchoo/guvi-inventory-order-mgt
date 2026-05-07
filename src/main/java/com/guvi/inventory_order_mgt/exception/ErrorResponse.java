package com.guvi.inventory_order_mgt.exception;

import java.time.LocalDateTime;

// This is not an exception — it's the standardised error payload returned to the client whenever an exception is thrown
public class ErrorResponse {

    private int status;
    private String message;
    private String path; // which endpoint triggered the error
    private LocalDateTime timestamp;

    // Constructors
    public ErrorResponse() {
    }

    public ErrorResponse(int status, String message, String path, LocalDateTime timestamp) {
        this.status = status;
        this.message = message;
        this.path = path;
        this.timestamp = timestamp;
    }

    // Getters & Setters
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
