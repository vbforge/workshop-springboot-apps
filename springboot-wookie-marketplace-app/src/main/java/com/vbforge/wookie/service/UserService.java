package com.vbforge.wookie.service;

import com.vbforge.wookie.entity.User;
import com.vbforge.wookie.entity.Roles;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for User management operations.
 * Defines all business logic methods for handling users.
 */
public interface UserService {
    

    User createUser(User user);

    User registerUser(String authorPseudonym, String rawPassword);

    Optional<User> getUserById(Long userId);

    Optional<User> findUserByAuthorPseudonym(String authorPseudonym);

    Optional<User> updateAuthorPseudonym(String currentPseudonym, String newPseudonym);

    boolean deleteUserById(Long userId);

    List<User> findAllUsers();

    Optional<User> findUserByBookTitle(String title);

    User updateUserRole(Long userId, Roles role);

    boolean existsByPseudonym(String authorPseudonym);
}