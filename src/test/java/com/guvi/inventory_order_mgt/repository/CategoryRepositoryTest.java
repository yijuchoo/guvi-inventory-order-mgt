package com.guvi.inventory_order_mgt.repository;

import com.guvi.inventory_order_mgt.model.Category;
import com.guvi.inventory_order_mgt.repo.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setName("Electronics");
        categoryRepository.save(testCategory);
    }

    @Test
    void findByName_ShouldReturnCategory_WhenNameExists() {
        Optional<Category> result = categoryRepository.findByName("Electronics");
        assertTrue(result.isPresent());
        assertEquals("Electronics", result.get().getName());
    }

    @Test
    void findByName_ShouldReturnEmpty_WhenNameNotFound() {
        Optional<Category> result = categoryRepository.findByName("NonExistent");
        assertFalse(result.isPresent());
    }

    @Test
    void findByName_ShouldBeCaseSensitive() {
        // findByName (not IgnoreCase) — uppercase should NOT match
        Optional<Category> result = categoryRepository.findByName("ELECTRONICS");
        assertFalse(result.isPresent());
    }

    @Test
    void save_ShouldPersistCategory_WithGeneratedId() {
        assertNotNull(testCategory.getId());
    }

    @Test
    void save_ShouldThrowException_WhenDuplicateName() {
        Category duplicate = new Category();
        duplicate.setName("Electronics");

        assertThrows(Exception.class,
                () -> categoryRepository.saveAndFlush(duplicate));
    }

    @Test
    void delete_ShouldRemoveCategory_WhenExists() {
        categoryRepository.delete(testCategory);
        Optional<Category> result = categoryRepository
                .findByName("Electronics");
        assertFalse(result.isPresent());
    }
}