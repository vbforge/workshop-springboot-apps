package com.vbforge.wookie.security;

import com.vbforge.wookie.entity.Roles;
import com.vbforge.wookie.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "SECRET_KEY", "testSecretKeyForJWTGenerationThatIsAtLeast32CharsLong");
        ReflectionTestUtils.setField(jwtService, "EXPIRATION_TIME", 3600000L); // 1 hour

        testUser = User.builder()
                .userId(1L)
                .authorPseudonym("test_user")
                .role(Roles.USER)
                .build();
    }

    @Test
    void generateToken_Success() {
        // When
        String token = jwtService.generateToken(testUser);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void extractUsername_Success() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        String username = jwtService.extractUsername(token);

        // Then
        assertThat(username).isEqualTo("test_user");
    }

    @Test
    void validateToken_ValidToken_ReturnsTrue() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        Boolean isValid = jwtService.validateToken(token, testUser);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_WrongUser_ReturnsFalse() {
        // Given
        String token = jwtService.generateToken(testUser);
        User wrongUser = User.builder()
                .authorPseudonym("wrong_user")
                .build();

        // When
        Boolean isValid = jwtService.validateToken(token, wrongUser);

        // Then
        assertThat(isValid).isFalse();
    }
}