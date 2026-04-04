package com.guvi.inventory_order_mgt.controller;

import com.guvi.inventory_order_mgt.dto.ProductRequest;
import com.guvi.inventory_order_mgt.dto.ProductResponse;
import com.guvi.inventory_order_mgt.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

/*
Endpoints
`POST, PUT, DELETE, GET /api/products`, search, filter by category, low-stock
*/
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // GET /api/products?page=0&size=10&sort=price,asc
    // ADMIN sees all products, USER sees only ACTIVE products
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProducts(
            // /products?page=1&size=5&sortBy=name&sortDir=desc
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Authentication authentication) {

        // Build sorting logic
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        // Create Pageable object
        Pageable pageable = PageRequest.of(page, size, sort);

        // Get user role from Authentication
        // authentication.getAuthorities() → gets user roles (e.g. ROLE_USER, ROLE_ADMIN)
        // Convert each role to string
        // Check if any role equals "ROLE_ADMIN"
        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
        // .anyMatch(a -> "ROLE_ADMIN".equals(a));
        // Admin Gets ALL products (including inactive/disabled ones)
        if (isAdmin) {
            return ResponseEntity.ok(productService.getAllProducts(pageable));
        }
        // Non-Admin Gets only active products
        return ResponseEntity.ok(productService.getActiveProducts(pageable));
    }

    // GET /api/products/search?name=shoe&page=0&size=10
    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> searchByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.searchProductsByName(name, pageable));
    }

    // GET /api/products/category/{categoryId}?page=0&size=10&sort=price,asc
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductResponse>> getByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "price") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId, pageable));
    }

    // GET /api/products/low-stock?threshold=10&page=0&size=10
    @GetMapping("/low-stock")
    public ResponseEntity<Page<ProductResponse>> getLowStockProducts(
            @RequestParam(defaultValue = "10") int threshold,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getLowStockProducts(threshold, pageable));
    }
}
