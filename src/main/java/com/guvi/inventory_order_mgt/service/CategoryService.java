package com.guvi.inventory_order_mgt.service;

import com.guvi.inventory_order_mgt.dto.CategoryRequest;
import com.guvi.inventory_order_mgt.dto.CategoryResponse;
import com.guvi.inventory_order_mgt.exception.DuplicateResourceException;
import com.guvi.inventory_order_mgt.exception.ResourceNotFoundException;
import com.guvi.inventory_order_mgt.model.Category;
import com.guvi.inventory_order_mgt.repo.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

// Purpose: CRUD for categories, duplicate name check
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // Create Category
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.findByName(request.getName()).isPresent()) {
            throw new DuplicateResourceException(
                    "Category already exists: " + request.getName());
        }

        Category category = new Category();
        category.setName(request.getName());
        categoryRepository.save(category);

        return toResponse(category);
    }

    // Update Category
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with ID: " + id));

        // Check name conflict only if name is actually changing
        if (!category.getName().equalsIgnoreCase(request.getName()) &&
                categoryRepository.findByName(request.getName()).isPresent()) {
            throw new DuplicateResourceException(
                    "Category already exists: " + request.getName());
        }

        category.setName(request.getName());
        categoryRepository.save(category);

        return toResponse(category);
    }

    // Delete Category
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with ID: " + id));

        categoryRepository.delete(category);
    }

    // Get All Categories
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Get Category By ID
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with ID: " + id));

        return toResponse(category);
    }

    // Mapper
    // Converts a Category object into a CategoryResponse object
    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}
