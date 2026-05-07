package com.guvi.inventory_order_mgt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guvi.inventory_order_mgt.bootstrap.UserSeeder;
import com.guvi.inventory_order_mgt.dto.ProductRequest;
import com.guvi.inventory_order_mgt.dto.ProductResponse;
import com.guvi.inventory_order_mgt.enums.ProductStatus;
import com.guvi.inventory_order_mgt.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private UserSeeder userSeeder;

    private ProductResponse mockProductResponse() {
        return new ProductResponse(
                1L, "Wireless Headphones", "Noise cancelling",
                99.99, 50, ProductStatus.ACTIVE, Set.of());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_ShouldReturn201_WhenAdminAndValidRequest() throws Exception {
        ProductRequest request = new ProductRequest(
                "Wireless Headphones", "Noise cancelling",
                99.99, 50, ProductStatus.ACTIVE, Set.of(1L));

        when(productService.createProduct(any(ProductRequest.class)))
                .thenReturn(mockProductResponse());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Wireless Headphones"))
                .andExpect(jsonPath("$.price").value(99.99));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createProduct_ShouldReturn403_WhenUser() throws Exception {
        ProductRequest request = new ProductRequest(
                "Wireless Headphones", "Noise cancelling",
                99.99, 50, ProductStatus.ACTIVE, Set.of(1L));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateProduct_ShouldReturn200_WhenAdminAndValidRequest() throws Exception {
        ProductRequest request = new ProductRequest(
                "Updated Headphones", "Updated description",
                129.99, 45, ProductStatus.ACTIVE, Set.of(1L));

        when(productService.updateProduct(eq(1L), any(ProductRequest.class)))
                .thenReturn(mockProductResponse());

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_ShouldReturn204_WhenAdminAndExists() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getProductById_ShouldReturn200_WhenAuthenticated() throws Exception {
        when(productService.getProductById(1L))
                .thenReturn(mockProductResponse());

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Wireless Headphones"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getProducts_ShouldReturnActiveProducts_WhenUser() throws Exception {
        Page<ProductResponse> page = new PageImpl<>(List.of(mockProductResponse()));

        when(productService.getActiveProducts(any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getProducts_ShouldReturnAllProducts_WhenAdmin() throws Exception {
        Page<ProductResponse> page = new PageImpl<>(List.of(mockProductResponse()));

        when(productService.getAllProducts(any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void searchByName_ShouldReturn200_WithMatchingProducts() throws Exception {
        Page<ProductResponse> page = new PageImpl<>(List.of(mockProductResponse()));

        when(productService.searchProductsByName(eq("headphone"), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/products/search")
                        .param("name", "headphone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getByCategory_ShouldReturn200_WithFilteredProducts() throws Exception {
        Page<ProductResponse> page = new PageImpl<>(List.of(mockProductResponse()));

        when(productService.getProductsByCategory(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/products/category/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getLowStockProducts_ShouldReturn200_WhenAdmin() throws Exception {
        Page<ProductResponse> page = new PageImpl<>(List.of(mockProductResponse()));

        when(productService.getLowStockProducts(eq(10), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/products/low-stock")
                        .param("threshold", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }
}
