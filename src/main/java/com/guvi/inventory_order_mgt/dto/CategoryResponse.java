package com.guvi.inventory_order_mgt.dto;

public class CategoryResponse {
    // Purpose: Return category data

    private Long id;
    private String name;

    // Constructors
    public CategoryResponse() {
    }

    public CategoryResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
