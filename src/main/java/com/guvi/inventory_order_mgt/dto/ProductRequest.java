package com.guvi.inventory_order_mgt.dto;

import com.guvi.inventory_order_mgt.enums.ProductStatus;
import jakarta.validation.constraints.*;

import java.util.Set;

public class ProductRequest {
    // Purpose: Create/update product

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @Positive(message = "Price must be greater than 0")
    private double price;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    private int stockQuantity;

    @NotNull(message = "Status is required")
    private ProductStatus status;

    @NotEmpty(message = "At least one category is required")
    private Set<Long> categoryIds;
    /*
    A list of Category IDs that the client (frontend) sends when creating or updating a product.
    {
        "name": "Laptop",
        "price": 1200,
        "categoryIds": [1, 2]
    }
    */

    // Constructors
    public ProductRequest() {
    }

    public ProductRequest(String name, String description, double price, int stockQuantity, ProductStatus status,
                          Set<Long> categoryIds) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.status = status;
        this.categoryIds = categoryIds;
    }

    // Getters & Setters
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

    public Set<Long> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(Set<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }
}
