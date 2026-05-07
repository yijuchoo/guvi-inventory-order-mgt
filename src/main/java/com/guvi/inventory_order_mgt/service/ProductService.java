package com.guvi.inventory_order_mgt.service;

import com.guvi.inventory_order_mgt.dto.CategoryResponse;
import com.guvi.inventory_order_mgt.dto.ProductRequest;
import com.guvi.inventory_order_mgt.dto.ProductResponse;
import com.guvi.inventory_order_mgt.enums.ProductStatus;
import com.guvi.inventory_order_mgt.exception.ResourceNotFoundException;
import com.guvi.inventory_order_mgt.model.Category;
import com.guvi.inventory_order_mgt.model.Product;
import com.guvi.inventory_order_mgt.repo.CategoryRepository;
import com.guvi.inventory_order_mgt.repo.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

// Purpose: CRUD, search, filter by category, low stock
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product();
        mapRequestToProduct(request, product);
        productRepository.save(product);
        return toResponse(product);
    }

    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with ID: " + id));

        mapRequestToProduct(request, product);
        productRepository.save(product);
        return toResponse(product);
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with ID: " + id));

        return toResponse(product);
    }

    // Get all active products — for USER
    public Page<ProductResponse> getActiveProducts(Pageable pageable) {
        return productRepository.findByStatus(ProductStatus.ACTIVE, pageable)
                .map(this::toResponse);
    }

    // Get all products regardless of status — for ADMIN
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(this::toResponse);
    }

    // Search by name — for USER (active only)
    public Page<ProductResponse> searchProductsByName(String name, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(this::toResponse);
    }

    // Filter by category — for USER (active only)
    public Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndStatus(
                        categoryId, ProductStatus.ACTIVE, pageable)
                .map(this::toResponse);
    }

    // Low stock — for ADMIN
    public Page<ProductResponse> getLowStockProducts(int threshold, Pageable pageable) {
        return productRepository.findLowStockProducts(threshold, pageable)
                .map(this::toResponse);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with ID: " + id));

        productRepository.delete(product);
    }

    // Shared mapper — maps ProductRequest fields onto a Product entity
    private void mapRequestToProduct(ProductRequest request, Product product) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setStatus(request.getStatus());

        // Resolve category IDs to Category entities
        Set<Category> categories = new HashSet<>();
        for (Long categoryId : request.getCategoryIds()) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category not found with ID: " + categoryId));
            categories.add(category);
        }
        product.setCategories(categories);
    }

    // Mapper — Product entity to ProductResponse DTO
    public ProductResponse toResponse(Product product) {
        Set<CategoryResponse> categoryResponses = product.getCategories()
                .stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName()))
                .collect(Collectors.toSet());

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getStatus(),
                categoryResponses
        );
    }
}
