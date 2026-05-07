package com.guvi.inventory_order_mgt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guvi.inventory_order_mgt.bootstrap.UserSeeder;
import com.guvi.inventory_order_mgt.dto.CategoryRequest;
import com.guvi.inventory_order_mgt.dto.CategoryResponse;
import com.guvi.inventory_order_mgt.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private UserSeeder userSeeder;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_ShouldReturn201_WhenAdminAndValidRequest() throws Exception {
        CategoryRequest request = new CategoryRequest("Electronics");
        CategoryResponse response = new CategoryResponse(1L, "Electronics");

        when(categoryService.createCategory(any(CategoryRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createCategory_ShouldReturn403_WhenUser() throws Exception {
        CategoryRequest request = new CategoryRequest("Electronics");

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createCategory_ShouldReturn403_WhenUnauthenticated() throws Exception {
        CategoryRequest request = new CategoryRequest("Electronics");

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCategory_ShouldReturn200_WhenAdminAndValidRequest() throws Exception {
        CategoryRequest request = new CategoryRequest("Updated Electronics");
        CategoryResponse response = new CategoryResponse(1L, "Updated Electronics");

        when(categoryService.updateCategory(eq(1L), any(CategoryRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Electronics"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_ShouldReturn204_WhenAdminAndExists() throws Exception {
        doNothing().when(categoryService).deleteCategory(1L);

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCategories_ShouldReturn200_WithCategoryList() throws Exception {
        when(categoryService.getAllCategories())
                .thenReturn(List.of(
                        new CategoryResponse(1L, "Electronics"),
                        new CategoryResponse(2L, "Accessories")));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Electronics"))
                .andExpect(jsonPath("$[1].name").value("Accessories"));
    }
}
