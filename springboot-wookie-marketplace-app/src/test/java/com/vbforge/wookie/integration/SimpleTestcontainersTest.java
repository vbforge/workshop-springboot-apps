package com.vbforge.wookie.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
public class SimpleTestcontainersTest {

    @Value("${spring.datasource.username.value}")
    private static String username;

    @Value("${spring.datasource.password.value}")
    private static String password;


    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Use your existing MySQL
        registry.add("spring.datasource.url", () -> "jdbc:mysql://localhost:3306/wookie_books?useSSL=false&allowPublicKeyRetrieval=true");
        registry.add("spring.datasource.username", () -> username);
        registry.add("spring.datasource.password", () -> password);
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Test
    void testDatabaseConnection() {
        System.out.println("Testing with existing MySQL database");
        // This will use your real database
    }
}