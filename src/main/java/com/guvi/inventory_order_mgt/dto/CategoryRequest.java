package com.guvi.inventory_order_mgt.dto;

import jakarta.validation.constraints.NotBlank;

public class CategoryRequest {
    // Purpose: Create/update category

    @NotBlank(message = "Category name is required")
    private String name;

    // Constructors
    public CategoryRequest() {
    }

    public CategoryRequest(String name) {
        this.name = name;
    }

    // Getters & Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
