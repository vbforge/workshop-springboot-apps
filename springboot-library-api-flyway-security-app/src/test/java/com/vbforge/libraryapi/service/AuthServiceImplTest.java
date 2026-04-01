package com.vbforge.libraryapi.service;

import com.vbforge.libraryapi.dto.request.LoginRequest;
import com.vbforge.libraryapi.dto.request.SignupRequest;
import com.vbforge.libraryapi.dto.response.AuthResponse;
import com.vbforge.libraryapi.entity.Role;
import com.vbforge.libraryapi.entity.User;
import com.vbforge.libraryapi.exception.custom.BadRequestException;
import com.vbforge.libraryapi.exception.custom.UserAlreadyExistException;
import com.vbforge.libraryapi.repository.UserRepository;
import com.vbforge.libraryapi.security.JwtService;
import com.vbforge.libraryapi.security.UserDetailsServiceImpl;
import com.vbforge.libraryapi.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Unit Tests")
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private UserDetailsServiceImpl userDetailsService;

    @InjectMocks
    private AuthServiceImpl authService;

    // --- Fixtures ---
    private User testUser;
    private SignupRequest signupRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("johndoe")
                .email("john@example.com")
                .password("$2a$10$hashedPassword")
                .role(Role.USER)
                .build();

        signupRequest = new SignupRequest();
        signupRequest.setUsername("johndoe");
        signupRequest.setEmail("john@example.com");
        signupRequest.setPassword("securePass123");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("johndoe");
        loginRequest.setPassword("securePass123");
    }

    // =========================================================
    //  signup()
    // =========================================================
    @Nested
    @DisplayName("signup()")
    class SignupTests {

        @Test
        @DisplayName("should register a new user and return AuthResponse with token")
        void signup_success() {
            // Arrange
            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.empty());
            when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
            when(passwordEncoder.encode("securePass123")).thenReturn("$2a$10$hashed");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userDetailsService.loadUserByUsername("johndoe")).thenReturn(testUser);
            when(jwtService.generateToken(testUser)).thenReturn("jwt-token");
            when(jwtService.extractExpirationAsLocalDateTime("jwt-token"))
                    .thenReturn(LocalDateTime.now().plusDays(1));

            // Act
            AuthResponse response = authService.signup(signupRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getUsername()).isEqualTo("johndoe");
            assertThat(response.getEmail()).isEqualTo("john@example.com");
            assertThat(response.getRole()).isEqualTo(Role.USER);

            verify(userRepository).save(argThat(u ->
                    u.getUsername().equals("johndoe") &&
                    u.getRole() == Role.USER
            ));
        }

        @Test
        @DisplayName("should throw UserAlreadyExistException when username is taken")
        void signup_duplicateUsername_throwsException() {
            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));

            assertThatThrownBy(() -> authService.signup(signupRequest))
                    .isInstanceOf(UserAlreadyExistException.class)
                    .hasMessageContaining("johndoe");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw UserAlreadyExistException when email is already in use")
        void signup_duplicateEmail_throwsException() {
            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.empty());
            when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.signup(signupRequest))
                    .isInstanceOf(UserAlreadyExistException.class)
                    .hasMessageContaining("john@example.com");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should sanitize username and normalize email to lowercase")
        void signup_sanitizesAndNormalizesInput() {
            SignupRequest dirtyRequest = new SignupRequest();
            dirtyRequest.setUsername("  JohnDoe  ");
            dirtyRequest.setEmail("  JOHN@EXAMPLE.COM  ");
            dirtyRequest.setPassword("pass");

            when(userRepository.findByUsername("JohnDoe")).thenReturn(Optional.empty());
            when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("hashed");
            when(userRepository.save(any())).thenReturn(testUser);
            when(userDetailsService.loadUserByUsername(anyString())).thenReturn(testUser);
            when(jwtService.generateToken(any())).thenReturn("token");
            when(jwtService.extractExpirationAsLocalDateTime(any())).thenReturn(LocalDateTime.now().plusDays(1));

            authService.signup(dirtyRequest);

            // Email should be lowercased and trimmed
            verify(userRepository).existsByEmail("john@example.com");
        }

        @Test
        @DisplayName("should always assign USER role on signup")
        void signup_alwaysAssignsUserRole() {
            when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("hashed");
            when(userRepository.save(any())).thenReturn(testUser);
            when(userDetailsService.loadUserByUsername(anyString())).thenReturn(testUser);
            when(jwtService.generateToken(any())).thenReturn("token");
            when(jwtService.extractExpirationAsLocalDateTime(any())).thenReturn(LocalDateTime.now().plusDays(1));

            authService.signup(signupRequest);

            verify(userRepository).save(argThat(u -> u.getRole() == Role.USER));
        }
    }

    // =========================================================
    //  login()
    // =========================================================
    @Nested
    @DisplayName("login()")
    class LoginTests {

        @Test
        @DisplayName("should return AuthResponse with valid JWT on successful login")
        void login_success() {
            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("securePass123", testUser.getPassword())).thenReturn(true);
            when(userDetailsService.loadUserByUsername("johndoe")).thenReturn(testUser);
            when(jwtService.generateToken(testUser)).thenReturn("jwt-token");
            when(jwtService.extractExpirationAsLocalDateTime("jwt-token"))
                    .thenReturn(LocalDateTime.now().plusDays(1));

            AuthResponse response = authService.login(loginRequest);

            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getUsername()).isEqualTo("johndoe");
        }

        @Test
        @DisplayName("should throw BadRequestException for unknown username")
        void login_unknownUsername_throwsException() {
            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Invalid username");

            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }

        @Test
        @DisplayName("should throw BadRequestException for wrong password")
        void login_wrongPassword_throwsException() {
            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("securePass123", testUser.getPassword())).thenReturn(false);

            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Invalid password");

            verify(jwtService, never()).generateToken(any());
        }

        @Test
        @DisplayName("should sanitize username input before lookup")
        void login_sanitizesUsername() {
            loginRequest.setUsername("  johndoe  ");
            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            when(userDetailsService.loadUserByUsername(anyString())).thenReturn(testUser);
            when(jwtService.generateToken(any())).thenReturn("token");
            when(jwtService.extractExpirationAsLocalDateTime(any())).thenReturn(LocalDateTime.now().plusDays(1));

            authService.login(loginRequest);

            verify(userRepository).findByUsername("johndoe");
        }
    }
}
