package com.guvi.inventory_order_mgt.repo;

import com.guvi.inventory_order_mgt.enums.ProductStatus;
import com.guvi.inventory_order_mgt.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // Search by name (case-insensitive)
    // Find all Product records where the name contains a given keyword, ignoring uppercase/lowercase differences,
    // and return the results in paginated form.
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Filter by status with pagination
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    // Filter by category ID with pagination
    @Query("SELECT p FROM Product p JOIN p.categories c WHERE c.id = :categoryId")
    Page<Product> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    // Filter by category ID and status (for USER — only ACTIVE products)
    @Query("SELECT p FROM Product p JOIN p.categories c WHERE c.id = :categoryId AND p.status = :status")
    Page<Product> findByCategoryIdAndStatus(@Param("categoryId") Long categoryId,
                                            @Param("status") ProductStatus status,
                                            Pageable pageable);

    // Low-stock products below a given threshold
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= :threshold")
    Page<Product> findLowStockProducts(@Param("threshold") int threshold, Pageable pageable);

}
