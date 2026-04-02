package com.vbforge.wookie.service.impl;

import com.vbforge.wookie.entity.Roles;
import com.vbforge.wookie.entity.User;
import com.vbforge.wookie.exception.DuplicateResourceException;
import com.vbforge.wookie.exception.ResourceNotFoundException;
import com.vbforge.wookie.repository.BookRepository;
import com.vbforge.wookie.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1L)
                .authorPseudonym("test_author")
                .authorPassword("raw_password")
                .role(Roles.USER)
                .isActive(true)
                .build();
    }

    @Test
    void createUser_Success() {
        // Given
        when(userRepository.existsByAuthorPseudonym(testUser.getAuthorPseudonym())).thenReturn(false);
        when(passwordEncoder.encode(testUser.getAuthorPassword())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User createdUser = userService.createUser(testUser);

        // Then
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getAuthorPseudonym()).isEqualTo("test_author");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_DuplicatePseudonym_ThrowsException() {
        // Given
        when(userRepository.existsByAuthorPseudonym(testUser.getAuthorPseudonym())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(testUser))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
        
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> foundUser = userService.getUserById(1L);

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUserId()).isEqualTo(1L);
        assertThat(foundUser.get().getAuthorPseudonym()).isEqualTo("test_author");
    }

    @Test
    void getUserById_NotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<User> foundUser = userService.getUserById(999L);

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    void existsByPseudonym_ReturnsTrue() {
        // Given
        when(userRepository.existsByAuthorPseudonym("test_author")).thenReturn(true);

        // When
        boolean exists = userService.existsByPseudonym("test_author");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void deleteUserById_SoftDelete() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        boolean deleted = userService.deleteUserById(1L);

        // Then
        assertThat(deleted).isTrue();
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void deleteUserById_NotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        boolean deleted = userService.deleteUserById(999L);

        // Then
        assertThat(deleted).isFalse();
        verify(userRepository, never()).save(any(User.class));
    }
}