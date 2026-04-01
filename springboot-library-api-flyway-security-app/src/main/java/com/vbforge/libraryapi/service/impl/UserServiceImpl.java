package com.vbforge.libraryapi.service.impl;

import com.vbforge.libraryapi.config.SecurityConfig;
import com.vbforge.libraryapi.dto.request.UpdateRoleRequest;
import com.vbforge.libraryapi.dto.request.UpdateUserRequest;
import com.vbforge.libraryapi.dto.response.UserResponse;
import com.vbforge.libraryapi.entity.Role;
import com.vbforge.libraryapi.entity.User;
import com.vbforge.libraryapi.exception.custom.BadRequestException;
import com.vbforge.libraryapi.exception.custom.RoleUpdateException;
import com.vbforge.libraryapi.exception.custom.UserNotFoundException;
import com.vbforge.libraryapi.repository.UserRepository;
import com.vbforge.libraryapi.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.owasp.encoder.Encode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SecurityConfig securityConfig;

    // === Helper Methods ===
    private String sanitizeInput(String input) {
        return Encode.forHtml(input.trim());
    }

    private void validatePassword(String password) {
        if (!password.matches("^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=!]).{8,}$")) {
            throw new BadRequestException(
                "Password must contain at least one digit, one letter, and one special character"
            );
        }
    }

    // === Core Methods ===
    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("User ID must be a positive number");
        }
        log.info("Fetching user by ID: {}", id);
        return UserResponse.from(
            userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id))
        );
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        if (id == null || id <= 0 || request == null) {
            throw new BadRequestException("User ID must be positive and request must not be null");
        }

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        // Update username (if provided)
        if (request.getUsername() != null && !existingUser.getUsername().equals(request.getUsername())) {
            existingUser.setUsername(sanitizeInput(request.getUsername()));
        }

        // Update email (if provided)
        if (request.getEmail() != null && !existingUser.getEmail().equals(request.getEmail())) {
            existingUser.setEmail(request.getEmail().trim().toLowerCase());
        }

        // Update password (if provided)
        if (request.getPassword() != null) {
            validatePassword(request.getPassword());
            existingUser.setPassword(securityConfig.passwordEncoder().encode(request.getPassword()));
        }

        log.info("Updated user: {} (ID: {})", existingUser.getUsername(), id);
        return UserResponse.from(userRepository.save(existingUser));
    }

    @Override
    @Transactional
    public UserResponse updateUserRole(Long id, UpdateRoleRequest request) {
        if (id == null || id <= 0 || request == null) {
            throw new RoleUpdateException("User ID must be positive and request must not be null");
        }
        if (request.getRole() == null || !List.of(Role.USER, Role.LIBRARIAN).contains(request.getRole())) {
            throw new RoleUpdateException("Invalid role: " + request.getRole());
        }

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        existingUser.setRole(request.getRole());
        log.info("Updated role for user: {} (ID: {}) to {}", existingUser.getUsername(), id, request.getRole());
        return UserResponse.from(userRepository.save(existingUser));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(Role role) {
        if (role == null) {
            throw new BadRequestException("Role must not be null");
        }
        log.info("Fetching users by role: {}", role);
        return userRepository.findByRole(role).stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUserById(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("User ID must be a positive number");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        log.info("Deleting user: {} (ID: {})", user.getUsername(), id);
        userRepository.deleteById(id);
    }
}