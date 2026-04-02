package com.vbforge.wookie.controller;

import com.vbforge.wookie.dto.request.UserCreateRequest;
import com.vbforge.wookie.dto.request.UserUpdateRequest;
import com.vbforge.wookie.dto.response.BookResponse;
import com.vbforge.wookie.dto.response.UserResponse;
import com.vbforge.wookie.entity.Roles;
import com.vbforge.wookie.entity.User;
import com.vbforge.wookie.exception.DuplicateResourceException;
import com.vbforge.wookie.exception.PermissionDeniedException;
import com.vbforge.wookie.exception.ResourceNotFoundException;
import com.vbforge.wookie.service.BookService;
import com.vbforge.wookie.service.UserService;
import com.vbforge.wookie.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final BookService bookService;

    @Value("${app.restricted.username}")
    private String RESTRICTED_USER_NAME;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserCreateRequest request) {
        log.info("Registering new user with pseudonym: {}", request.getAuthorPseudonym());
        
        if (userService.existsByPseudonym(request.getAuthorPseudonym())) {
            throw new DuplicateResourceException(
                "User with pseudonym '" + request.getAuthorPseudonym() + "' already exists"
            );
        }
        
        User user = User.builder()
                .authorPseudonym(request.getAuthorPseudonym())
                .authorPassword(request.getAuthorPassword())
                .role(Roles.USER)
                .isActive(true)
                .build();
        
        if (RESTRICTED_USER_NAME != null && RESTRICTED_USER_NAME.equals(request.getAuthorPseudonym())) {
            user.setRole(Roles.RESTRICTED_USER);
            log.warn("Restricted user created: {}", RESTRICTED_USER_NAME);
        }
        
        User savedUser = userService.createUser(user);
        log.info("User registered successfully with ID: {}", savedUser.getUserId());
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapToUserResponse(savedUser));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        log.debug("Fetching user by ID: {}", userId);
        
        // Check permission
        if (!SecurityUtils.isOwnerOrAdmin(userId)) {
            throw new PermissionDeniedException("You don't have permission to access this user's data");
        }
        
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        return ResponseEntity.ok(mapToUserResponse(user));
    }

    @GetMapping("/pseudonym/{authorPseudonym}")
    public ResponseEntity<UserResponse> findUserByAuthorPseudonym(@PathVariable String authorPseudonym) {
        log.debug("Fetching user by pseudonym: {}", authorPseudonym);
        
        User user = userService.findUserByAuthorPseudonym(authorPseudonym)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "User not found with pseudonym: " + authorPseudonym
                ));
        
        // Check permission - users can only view their own profile or admin can view any
        if (!SecurityUtils.isOwnerOrAdmin(user.getUserId())) {
            throw new PermissionDeniedException("You don't have permission to access this user's data");
        }
        
        return ResponseEntity.ok(mapToUserResponse(user));
    }

    @PutMapping("/pseudonym/{currentPseudonym}")
    public ResponseEntity<UserResponse> updateAuthorPseudonym(
            @PathVariable String currentPseudonym,
            @RequestParam String newPseudonym) {
        
        log.info("Updating pseudonym from '{}' to '{}'", currentPseudonym, newPseudonym);
        
        // Get the user first
        User existingUser = userService.findUserByAuthorPseudonym(currentPseudonym)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "User not found with pseudonym: " + currentPseudonym
                ));
        
        // Check permission
        if (!SecurityUtils.isOwnerOrAdmin(existingUser.getUserId())) {
            throw new PermissionDeniedException("You don't have permission to update this user");
        }
        
        User updatedUser = userService.updateAuthorPseudonym(currentPseudonym, newPseudonym)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "User not found with pseudonym: " + currentPseudonym
                ));
        
        return ResponseEntity.ok(mapToUserResponse(updatedUser));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request) {
        
        log.info("Updating user with ID: {}", userId);
        
        // Check permission
        if (!SecurityUtils.isOwnerOrAdmin(userId)) {
            throw new PermissionDeniedException("You don't have permission to update this user");
        }
        
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        if (request.getAuthorPseudonym() != null && !request.getAuthorPseudonym().equals(user.getAuthorPseudonym())) {
            if (userService.existsByPseudonym(request.getAuthorPseudonym())) {
                throw new DuplicateResourceException(
                    "Pseudonym '" + request.getAuthorPseudonym() + "' is already taken"
                );
            }
            user.setAuthorPseudonym(request.getAuthorPseudonym());
        }
        
        if (request.getAuthorPassword() != null && !request.getAuthorPassword().isEmpty()) {
            user.setAuthorPassword(request.getAuthorPassword());
        }
        
        User updatedUser = userService.createUser(user);
        return ResponseEntity.ok(mapToUserResponse(updatedUser));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long userId) {
        log.info("Deleting user with ID: {}", userId);
        
        // Check permission
        if (!SecurityUtils.isOwnerOrAdmin(userId)) {
            throw new PermissionDeniedException("You don't have permission to delete this user");
        }
        
        boolean deleted = userService.deleteUserById(userId);
        if (!deleted) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> findAllUsers() {
        // Only admins can list all users
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null || currentUser.getRole() != Roles.SUPER_ADMIN) {
            throw new PermissionDeniedException("Only administrators can list all users");
        }
        
        List<UserResponse> users = userService.findAllUsers().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(users);
    }

    @GetMapping("/book/{title}")
    public ResponseEntity<UserResponse> findUserByBookTitle(@PathVariable String title) {
        log.debug("Finding user by book title: {}", title);
        
        User user = userService.findUserByBookTitle(title)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "No book found with title: " + title
                ));
        
        return ResponseEntity.ok(mapToUserResponse(user));
    }

    /**
     * Get current user's books (convenience method)
     * GET /api/user/me/books
     */
    @GetMapping("/me/books")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookResponse>> getMyBooks() {
        String currentUser = SecurityUtils.getCurrentUser().getAuthorPseudonym();
        log.debug("Fetching books for current user: {}", currentUser);

        List<BookResponse> books = bookService.findMyBooks(currentUser);
        return ResponseEntity.ok(books);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .authorPseudonym(user.getAuthorPseudonym())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}