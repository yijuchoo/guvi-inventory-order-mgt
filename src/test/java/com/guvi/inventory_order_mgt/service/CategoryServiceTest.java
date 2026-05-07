package com.guvi.inventory_order_mgt.service;

import com.guvi.inventory_order_mgt.dto.CategoryRequest;
import com.guvi.inventory_order_mgt.dto.CategoryResponse;
import com.guvi.inventory_order_mgt.exception.DuplicateResourceException;
import com.guvi.inventory_order_mgt.exception.ResourceNotFoundException;
import com.guvi.inventory_order_mgt.model.Category;
import com.guvi.inventory_order_mgt.repo.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category mockCategory;
    private CategoryRequest categoryRequest;

    @BeforeEach
    void setUp() {
        mockCategory = new Category();
        mockCategory.setId(1L);
        mockCategory.setName("Electronics");

        categoryRequest = new CategoryRequest("Electronics");
    }

    // ─── Create Tests ────

    @Test
    void createCategory_ShouldReturnCategoryResponse_WhenNameIsUnique() {
        when(categoryRepository.findByName(anyString()))
                .thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class)))
                .thenReturn(mockCategory);

        CategoryResponse response = categoryService.createCategory(categoryRequest);

        assertNotNull(response);
        assertEquals("Electronics", response.getName());
        assertEquals(1L, response.getId());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void createCategory_ShouldThrowDuplicateResourceException_WhenNameExists() {
        when(categoryRepository.findByName(anyString()))
                .thenReturn(Optional.of(mockCategory));

        assertThrows(DuplicateResourceException.class,
                () -> categoryService.createCategory(categoryRequest));

        verify(categoryRepository, never()).save(any(Category.class));
    }

    // ─── Update Tests ───

    @Test
    void updateCategory_ShouldReturnUpdatedCategory_WhenValidRequest() {
        CategoryRequest updateRequest = new CategoryRequest("Updated Electronics");

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(mockCategory));
        when(categoryRepository.findByName(anyString()))
                .thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class)))
                .thenReturn(mockCategory);

        CategoryResponse response = categoryService.updateCategory(1L, updateRequest);

        assertNotNull(response);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void updateCategory_ShouldThrowResourceNotFoundException_WhenCategoryNotFound() {
        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.updateCategory(99L, categoryRequest));
    }

    @Test
    void updateCategory_ShouldThrowDuplicateResourceException_WhenNameTakenByOther() {
        Category otherCategory = new Category();
        otherCategory.setId(2L);
        otherCategory.setName("Accessories");

        CategoryRequest updateRequest = new CategoryRequest("Accessories");

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(mockCategory));
        when(categoryRepository.findByName("Accessories"))
                .thenReturn(Optional.of(otherCategory));

        assertThrows(DuplicateResourceException.class,
                () -> categoryService.updateCategory(1L, updateRequest));
    }

    // ─── Delete Tests ───

    @Test
    void deleteCategory_ShouldDeleteSuccessfully_WhenCategoryExists() {
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(mockCategory));

        assertDoesNotThrow(() -> categoryService.deleteCategory(1L));
        verify(categoryRepository, times(1)).delete(mockCategory);
    }

    @Test
    void deleteCategory_ShouldThrowResourceNotFoundException_WhenCategoryNotFound() {
        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.deleteCategory(99L));

        verify(categoryRepository, never()).delete(any(Category.class));
    }

    // ─── Get Tests ────

    @Test
    void getAllCategories_ShouldReturnListOfCategories() {
        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Accessories");

        when(categoryRepository.findAll())
                .thenReturn(List.of(mockCategory, category2));

        List<CategoryResponse> responses = categoryService.getAllCategories();

        assertEquals(2, responses.size());
        assertEquals("Electronics", responses.get(0).getName());
        assertEquals("Accessories", responses.get(1).getName());
    }

    @Test
    void getAllCategories_ShouldReturnEmptyList_WhenNoCategoriesExist() {
        when(categoryRepository.findAll()).thenReturn(List.of());

        List<CategoryResponse> responses = categoryService.getAllCategories();

        assertTrue(responses.isEmpty());
    }

    @Test
    void getCategoryById_ShouldReturnCategory_WhenExists() {
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(mockCategory));

        CategoryResponse response = categoryService.getCategoryById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Electronics", response.getName());
    }

    @Test
    void getCategoryById_ShouldThrowResourceNotFoundException_WhenNotFound() {
        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.getCategoryById(99L));
    }
}