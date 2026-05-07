package com.guvi.inventory_order_mgt.repository;

import com.guvi.inventory_order_mgt.enums.ProductStatus;
import com.guvi.inventory_order_mgt.model.Category;
import com.guvi.inventory_order_mgt.model.Product;
import com.guvi.inventory_order_mgt.repo.CategoryRepository;
import com.guvi.inventory_order_mgt.repo.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Product activeProduct;
    private Product inactiveProduct;
    private Category category;
    private PageRequest pageable;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setName("Electronics");
        categoryRepository.save(category);

        activeProduct = new Product();
        activeProduct.setName("Wireless Headphones");
        activeProduct.setPrice(99.99);
        activeProduct.setStockQuantity(50);
        activeProduct.setStatus(ProductStatus.ACTIVE);
        activeProduct.setCategories(new HashSet<>(Set.of(category)));
        productRepository.save(activeProduct);

        inactiveProduct = new Product();
        inactiveProduct.setName("Old Speaker");
        inactiveProduct.setPrice(49.99);
        inactiveProduct.setStockQuantity(5);
        inactiveProduct.setStatus(ProductStatus.INACTIVE);
        inactiveProduct.setCategories(new HashSet<>(Set.of(category)));
        productRepository.save(inactiveProduct);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void findByStatus_ShouldReturnOnlyActiveProducts() {
        Page<Product> result = productRepository
                .findByStatus(ProductStatus.ACTIVE, pageable);
        assertEquals(1, result.getTotalElements());
        assertEquals("Wireless Headphones", result.getContent().get(0).getName());
    }

    @Test
    void findByNameContainingIgnoreCase_ShouldReturnMatchingProducts() {
        Page<Product> result = productRepository
                .findByNameContainingIgnoreCase("headphone", pageable);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findByNameContainingIgnoreCase_ShouldBeCaseInsensitive() {
        Page<Product> result = productRepository
                .findByNameContainingIgnoreCase("HEADPHONE", pageable);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findByCategoryId_ShouldReturnProductsInCategory() {
        Page<Product> result = productRepository
                .findByCategoryId(category.getId(), pageable);
        assertEquals(2, result.getTotalElements());
    }

    @Test
    void findByCategoryIdAndStatus_ShouldReturnOnlyActiveProductsInCategory() {
        Page<Product> result = productRepository
                .findByCategoryIdAndStatus(
                        category.getId(), ProductStatus.ACTIVE, pageable);
        assertEquals(1, result.getTotalElements());
        assertEquals(ProductStatus.ACTIVE,
                result.getContent().get(0).getStatus());
    }

    @Test
    void findLowStockProducts_ShouldReturnProductsBelowThreshold() {
        Page<Product> result = productRepository
                .findLowStockProducts(10, pageable);
        assertEquals(1, result.getTotalElements());
        assertEquals("Old Speaker", result.getContent().get(0).getName());
    }
}