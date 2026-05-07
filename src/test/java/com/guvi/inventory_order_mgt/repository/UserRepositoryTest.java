package com.guvi.inventory_order_mgt.repository;

import com.guvi.inventory_order_mgt.enums.Role;
import com.guvi.inventory_order_mgt.model.User;
import com.guvi.inventory_order_mgt.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setPasswordHash("hashedPassword");
        testUser.setRole(Role.USER);
        userRepository.save(testUser);
    }

    @Test
    void findByEmailIgnoreCase_ShouldReturnUser_WhenEmailExists() {
        Optional<User> result = userRepository
                .findByEmailIgnoreCase("john@example.com");
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
    }

    @Test
    void findByEmailIgnoreCase_ShouldReturnUser_WhenEmailIsUpperCase() {
        Optional<User> result = userRepository
                .findByEmailIgnoreCase("JOHN@EXAMPLE.COM");
        assertTrue(result.isPresent());
    }

    @Test
    void findByEmailIgnoreCase_ShouldReturnEmpty_WhenEmailNotFound() {
        Optional<User> result = userRepository
                .findByEmailIgnoreCase("notfound@example.com");
        assertFalse(result.isPresent());
    }

    @Test
    void save_ShouldPersistUser_WithGeneratedId() {
        assertNotNull(testUser.getId());
    }

    @Test
    void save_ShouldThrowException_WhenDuplicateEmail() {
        User duplicate = new User();
        duplicate.setName("Jane Doe");
        duplicate.setEmail("john@example.com");
        duplicate.setPasswordHash("hashedPassword");
        duplicate.setRole(Role.USER);

        assertThrows(Exception.class,
                () -> userRepository.saveAndFlush(duplicate));
    }
}