package com.guvi.inventory_order_mgt.service;

import com.guvi.inventory_order_mgt.dto.AuthResponse;
import com.guvi.inventory_order_mgt.dto.LoginRequest;
import com.guvi.inventory_order_mgt.dto.RegisterRequest;
import com.guvi.inventory_order_mgt.enums.Role;
import com.guvi.inventory_order_mgt.exception.DuplicateResourceException;
import com.guvi.inventory_order_mgt.model.User;
import com.guvi.inventory_order_mgt.repo.UserRepository;
import com.guvi.inventory_order_mgt.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User mockUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("John Doe");
        mockUser.setEmail("john@example.com");
        mockUser.setPasswordHash("hashedPassword");
        mockUser.setRole(Role.USER);

        registerRequest = new RegisterRequest(
                "John Doe", "john@example.com", "password123");

        loginRequest = new LoginRequest(
                "john@example.com", "password123");
    }

    // ─── Register Tests ───

    @Test
    void register_ShouldReturnAuthResponse_WhenEmailNotTaken() {
        when(userRepository.findByEmailIgnoreCase(anyString()))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString()))
                .thenReturn("hashedPassword");
        when(userRepository.save(any(User.class)))
                .thenReturn(mockUser);
        when(jwtUtil.generateToken(any(), any()))
                .thenReturn("mockToken");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("mockToken", response.getToken());
        assertEquals("USER", response.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_ShouldThrowDuplicateResourceException_WhenEmailAlreadyExists() {
        when(userRepository.findByEmailIgnoreCase(anyString()))
                .thenReturn(Optional.of(mockUser));

        assertThrows(DuplicateResourceException.class,
                () -> authService.register(registerRequest));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_ShouldAlwaysAssignUserRole() {
        when(userRepository.findByEmailIgnoreCase(anyString()))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString()))
                .thenReturn("hashedPassword");
        when(userRepository.save(any(User.class)))
                .thenReturn(mockUser);
        when(jwtUtil.generateToken(any(), any()))
                .thenReturn("mockToken");

        AuthResponse response = authService.register(registerRequest);

        assertEquals("USER", response.getRole());
    }

    // ─── Login Tests ────

    @Test
    void login_ShouldReturnAuthResponse_WhenCredentialsAreValid() {
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken(
                        "john@example.com", "password123"));
        when(userRepository.findByEmailIgnoreCase(anyString()))
                .thenReturn(Optional.of(mockUser));
        when(jwtUtil.generateToken(any(), any()))
                .thenReturn("mockToken");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mockToken", response.getToken());
        assertEquals("USER", response.getRole());
        assertEquals(1L, response.getUserId());
    }

    @Test
    void login_ShouldThrowException_WhenCredentialsAreInvalid() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new RuntimeException("Bad credentials"));

        assertThrows(RuntimeException.class,
                () -> authService.login(loginRequest));
    }
}