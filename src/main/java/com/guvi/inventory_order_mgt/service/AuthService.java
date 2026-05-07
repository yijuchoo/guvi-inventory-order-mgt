package com.guvi.inventory_order_mgt.service;

import com.guvi.inventory_order_mgt.dto.AuthResponse;
import com.guvi.inventory_order_mgt.dto.LoginRequest;
import com.guvi.inventory_order_mgt.dto.RegisterRequest;
import com.guvi.inventory_order_mgt.enums.Role;
import com.guvi.inventory_order_mgt.exception.DuplicateResourceException;
import com.guvi.inventory_order_mgt.model.User;
import com.guvi.inventory_order_mgt.repo.UserRepository;
import com.guvi.inventory_order_mgt.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// Purpose: Register (USER only), Login — returns JWT
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    // AuthenticationManager -> Spring Security component responsible for verifying a user's credentials
    // It checks “Is this email + password correct?”
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.findByEmailIgnoreCase(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        // Build new user — role always hardcoded to USER on public registration
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        return new AuthResponse(user.getId(), token, user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        // Authenticate credentials — throws AuthenticationException if invalid
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Credentials valid — fetch user
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        return new AuthResponse(user.getId(), token, user.getRole().name());
    }
}