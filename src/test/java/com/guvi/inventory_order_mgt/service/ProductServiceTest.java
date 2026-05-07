package com.guvi.inventory_order_mgt.service;

import com.guvi.inventory_order_mgt.dto.ProductRequest;
import com.guvi.inventory_order_mgt.dto.ProductResponse;
import com.guvi.inventory_order_mgt.enums.ProductStatus;
import com.guvi.inventory_order_mgt.exception.ResourceNotFoundException;
import com.guvi.inventory_order_mgt.model.Category;
import com.guvi.inventory_order_mgt.model.Product;
import com.guvi.inventory_order_mgt.repo.CategoryRepository;
import com.guvi.inventory_order_mgt.repo.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private Product mockProduct;
    private Category mockCategory;
    private ProductRequest productRequest;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        mockCategory = new Category();
        mockCategory.setId(1L);
        mockCategory.setName("Electronics");

        mockProduct = new Product();
        mockProduct.setId(1L);
        mockProduct.setName("Wireless Headphones");
        mockProduct.setDescription("Noise cancelling");
        mockProduct.setPrice(99.99);
        mockProduct.setStockQuantity(50);
        mockProduct.setStatus(ProductStatus.ACTIVE);
        mockProduct.setCategories(new HashSet<>(Set.of(mockCategory)));

        productRequest = new ProductRequest(
                "Wireless Headphones", "Noise cancelling",
                99.99, 50, ProductStatus.ACTIVE, Set.of(1L));

        pageable = PageRequest.of(0, 10);
    }

    // ─── Create Tests ───

    @Test
    void createProduct_ShouldReturnProductResponse_WhenValidRequest() {
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(mockCategory));
        when(productRepository.save(any(Product.class)))
                .thenReturn(mockProduct);

        ProductResponse response = productService.createProduct(productRequest);

        assertNotNull(response);
        assertEquals("Wireless Headphones", response.getName());
        assertEquals(99.99, response.getPrice());
        assertEquals(ProductStatus.ACTIVE, response.getStatus());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void createProduct_ShouldThrowResourceNotFoundException_WhenCategoryNotFound() {
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.createProduct(productRequest));

        verify(productRepository, never()).save(any(Product.class));
    }

    // ─── Update Tests ───

    @Test
    void updateProduct_ShouldReturnUpdatedProduct_WhenValidRequest() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(mockProduct));
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(mockCategory));
        when(productRepository.save(any(Product.class)))
                .thenReturn(mockProduct);

        ProductResponse response = productService.updateProduct(1L, productRequest);

        assertNotNull(response);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateProduct_ShouldThrowResourceNotFoundException_WhenProductNotFound() {
        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.updateProduct(99L, productRequest));
    }

    // ─── Get Tests ───

    @Test
    void getProductById_ShouldReturnProduct_WhenExists() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(mockProduct));

        ProductResponse response = productService.getProductById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Wireless Headphones", response.getName());
    }

    @Test
    void getProductById_ShouldThrowResourceNotFoundException_WhenNotFound() {
        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.getProductById(99L));
    }

    @Test
    void getActiveProducts_ShouldReturnOnlyActiveProducts() {
        Page<Product> productPage = new PageImpl<>(List.of(mockProduct));
        when(productRepository.findByStatus(ProductStatus.ACTIVE, pageable))
                .thenReturn(productPage);

        Page<ProductResponse> responses = productService.getActiveProducts(pageable);

        assertEquals(1, responses.getTotalElements());
        assertEquals(ProductStatus.ACTIVE, responses.getContent().get(0).getStatus());
    }

    @Test
    void getAllProducts_ShouldReturnAllProducts_ForAdmin() {
        Product inactiveProduct = new Product();
        inactiveProduct.setId(2L);
        inactiveProduct.setName("Old Product");
        inactiveProduct.setStatus(ProductStatus.INACTIVE);
        inactiveProduct.setCategories(new HashSet<>());

        Page<Product> productPage = new PageImpl<>(
                List.of(mockProduct, inactiveProduct));
        when(productRepository.findAll(pageable))
                .thenReturn(productPage);

        Page<ProductResponse> responses = productService.getAllProducts(pageable);

        assertEquals(2, responses.getTotalElements());
    }

    @Test
    void searchProductsByName_ShouldReturnMatchingProducts() {
        Page<Product> productPage = new PageImpl<>(List.of(mockProduct));
        when(productRepository.findByNameContainingIgnoreCase("headphone", pageable))
                .thenReturn(productPage);

        Page<ProductResponse> responses =
                productService.searchProductsByName("headphone", pageable);

        assertEquals(1, responses.getTotalElements());
        assertTrue(responses.getContent().get(0).getName()
                .toLowerCase().contains("headphone"));
    }

    @Test
    void getLowStockProducts_ShouldReturnProductsBelowThreshold() {
        Product lowStockProduct = new Product();
        lowStockProduct.setId(2L);
        lowStockProduct.setName("USB Cable");
        lowStockProduct.setStockQuantity(3);
        lowStockProduct.setStatus(ProductStatus.ACTIVE);
        lowStockProduct.setCategories(new HashSet<>());

        Page<Product> productPage = new PageImpl<>(List.of(lowStockProduct));
        when(productRepository.findLowStockProducts(10, pageable))
                .thenReturn(productPage);

        Page<ProductResponse> responses =
                productService.getLowStockProducts(10, pageable);

        assertEquals(1, responses.getTotalElements());
        assertTrue(responses.getContent().get(0).getStockQuantity() <= 10);
    }

    // ─── Delete Tests ───

    @Test
    void deleteProduct_ShouldDeleteSuccessfully_WhenProductExists() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(mockProduct));

        assertDoesNotThrow(() -> productService.deleteProduct(1L));
        verify(productRepository, times(1)).delete(mockProduct);
    }

    @Test
    void deleteProduct_ShouldThrowResourceNotFoundException_WhenProductNotFound() {
        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.deleteProduct(99L));

        verify(productRepository, never()).delete(any(Product.class));
    }
}