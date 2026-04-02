package com.vbforge.wookie.service.impl;

import com.vbforge.wookie.entity.Book;
import com.vbforge.wookie.entity.Roles;
import com.vbforge.wookie.entity.User;
import com.vbforge.wookie.exception.DuplicateResourceException;
import com.vbforge.wookie.exception.ResourceNotFoundException;
import com.vbforge.wookie.repository.BookRepository;
import com.vbforge.wookie.repository.UserRepository;
import com.vbforge.wookie.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of UserService interface.
 * Handles all user-related business logic with transaction management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User createUser(User user) {
        log.debug("Creating new user with pseudonym: {}", user.getAuthorPseudonym());
        
        // Check for duplicate pseudonym
        if (userRepository.existsByAuthorPseudonym(user.getAuthorPseudonym())) {
            throw new DuplicateResourceException(
                "User with pseudonym '" + user.getAuthorPseudonym() + "' already exists"
            );
        }
        
        // Encode password before saving
        String encodedPassword = passwordEncoder.encode(user.getAuthorPassword());
        user.setAuthorPassword(encodedPassword);
        
        // Set default role if not specified
        if (user.getRole() == null) {
            user.setRole(Roles.USER);
        }
        
        // Set active status
        user.setIsActive(true);
        
        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getUserId());
        
        return savedUser;
    }

    @Override
    @Transactional
    public User registerUser(String authorPseudonym, String rawPassword) {
        log.debug("Registering new user: {}", authorPseudonym);
        
        User user = User.builder()
                .authorPseudonym(authorPseudonym)
                .authorPassword(rawPassword)
                .role(Roles.USER)
                .isActive(true)
                .build();
        
        return createUser(user);
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        log.debug("Fetching user by ID: {}", userId);
        return userRepository.findById(userId);
    }

    @Override
    public Optional<User> findUserByAuthorPseudonym(String authorPseudonym) {
        log.debug("Fetching user by pseudonym: {}", authorPseudonym);
        return userRepository.findByAuthorPseudonym(authorPseudonym);
    }

    @Override
    @Transactional
    public Optional<User> updateAuthorPseudonym(String currentPseudonym, String newPseudonym) {
        log.debug("Updating pseudonym from '{}' to '{}'", currentPseudonym, newPseudonym);
        
        return userRepository.findByAuthorPseudonym(currentPseudonym)
                .map(user -> {
                    // Check if new pseudonym is already taken
                    if (userRepository.existsByAuthorPseudonym(newPseudonym)) {
                        throw new DuplicateResourceException(
                            "Pseudonym '" + newPseudonym + "' is already taken"
                        );
                    }
                    
                    user.setAuthorPseudonym(newPseudonym);
                    User updatedUser = userRepository.save(user);
                    log.info("Pseudonym updated for user ID: {}", updatedUser.getUserId());
                    return updatedUser;
                });
    }

    @Override
    @Transactional
    public boolean deleteUserById(Long userId) {
        log.debug("Deleting user by ID: {}", userId);
        
        return userRepository.findById(userId)
                .map(user -> {
                    // Instead of hard delete, we can soft delete by setting inactive
                    user.setIsActive(false);
                    userRepository.save(user);
                    log.info("User deactivated with ID: {}", userId);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public List<User> findAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findUserByBookTitle(String title) {
        log.debug("Finding user by book title: {}", title);
        
        return bookRepository.findByTitle(title)
                .map(Book::getAuthor);
    }

    @Override
    @Transactional
    public User updateUserRole(Long userId, Roles role) {
        log.debug("Updating role for user ID: {} to {}", userId, role);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "User not found with ID: " + userId
                ));
        
        user.setRole(role);
        User updatedUser = userRepository.save(user);
        log.info("Role updated for user: {}", updatedUser.getAuthorPseudonym());
        
        return updatedUser;
    }

    @Override
    public boolean existsByPseudonym(String authorPseudonym) {
        return userRepository.existsByAuthorPseudonym(authorPseudonym);
    }
}