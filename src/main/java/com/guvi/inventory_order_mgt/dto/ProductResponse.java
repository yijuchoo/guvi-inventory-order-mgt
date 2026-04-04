package com.guvi.inventory_order_mgt.dto;

import com.guvi.inventory_order_mgt.enums.ProductStatus;

import java.util.Set;

public class ProductResponse {
    // Purpose: Return product data with categories

    private Long id;
    private String name;
    private String description;
    private double price;
    private int stockQuantity;
    private ProductStatus status;
    private Set<CategoryResponse> categories;
    // What it means:
    //      A product can belong to multiple categories
    //      Each category is also a DTO (CategoryResponse)

    // Constructors
    public ProductResponse() {
    }

    public ProductResponse(Long id, String name, String description, double price, int stockQuantity,
                           ProductStatus status,
                           Set<CategoryResponse> categories) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.status = status;
        this.categories = categories;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public Set<CategoryResponse> getCategories() {
        return categories;
    }

    public void setCategories(Set<CategoryResponse> categories) {
        this.categories = categories;
    }
}
