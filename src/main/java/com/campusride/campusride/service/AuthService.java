package com.campusride.campusride.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.campusride.campusride.dto.AuthResponse;
import com.campusride.campusride.dto.LoginRequest;
import com.campusride.campusride.model.User;
import com.campusride.campusride.repository.UserRepository;
import com.campusride.campusride.security.JwtUtil;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Register new user
    public AuthResponse register(User user) {

        // Check email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }

        // Check phone already exists
        if (userRepository.existsByPhone(user.getPhone())) {
            throw new RuntimeException("Phone already exists!");
        }

        // Encrypt password before saving
        user.setPassword(
            passwordEncoder.encode(user.getPassword())
        );

        // Save user to database
        User savedUser = userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(
            savedUser.getEmail(),
            savedUser.getRole().name(),
            savedUser.getId()
        );

        // Return token + user info
        return new AuthResponse(
            token,
            savedUser.getEmail(),
            savedUser.getRole().name(),
            savedUser.getId()
        );
    }

    // Login existing user
    public AuthResponse login(LoginRequest request) {

        // Find user by email
        User user = userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(() ->
                new RuntimeException("User not found!")
            );

        // Check password matches
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {
            throw new RuntimeException("Invalid password!");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(
            user.getEmail(),
            user.getRole().name(),
            user.getId()
        );

        // Return token + user info
        return new AuthResponse(
            token,
            user.getEmail(),
            user.getRole().name(),
            user.getId()
        );
    }
}