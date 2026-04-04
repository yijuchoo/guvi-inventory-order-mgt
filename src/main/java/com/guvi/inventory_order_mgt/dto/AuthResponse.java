package com.guvi.inventory_order_mgt.dto;

public class AuthResponse {
    /*
    userId is useful bcos USER needs to view their order history — the client could use userId to construct calls
    like GET /api/orders?userId=5
    However, a cleaner approach is to extract userId from the JWT token on the backend instead, so the client never
    needs to pass it explicitly
    */

    // Purpose: Returns JWT token + role
    private Long userId;
    private String token;
    private String role;

    // Constructors
    public AuthResponse() {
    }

    public AuthResponse(Long userId, String token, String role) {
        this.userId = userId;
        this.token = token;
        this.role = role;
    }

    // Getters & Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
