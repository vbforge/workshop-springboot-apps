package com.vbforge.bookapi.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * API Information and Health Check Controller
 */
@RestController
@RequestMapping("/api")
@Tag(name = "API Info", description = "API information and health check endpoints")
public class ApiInfoController {

    @Value("${spring.application.name:Book Management API}")
    private String applicationName;

    /**
     * Root endpoint - API welcome message
     */
    @GetMapping
    @Operation(summary = "API Welcome", description = "Returns API welcome message and information")
    public ResponseEntity<Map<String, Object>> apiRoot() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to Book Management REST API");
        response.put("version", "1.0.0");
        response.put("documentation", "/swagger-ui.html");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Health Check", description = "Returns API health status")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("application", applicationName);
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    /**
     * API version endpoint
     */
    @GetMapping("/version")
    @Operation(summary = "API Version", description = "Returns API version information")
    public ResponseEntity<Map<String, String>> version() {
        Map<String, String> response = new HashMap<>();
        response.put("version", "1.0.0");
        response.put("apiName", "Book Management REST API");
        response.put("javaVersion", System.getProperty("java.version"));
        response.put("springBootVersion", "3.5.7");

        return ResponseEntity.ok(response);
    }

    /**
     * Hidden endpoint - not shown in Swagger
     */
    @GetMapping("/internal")
    @Hidden
    public ResponseEntity<String> internalEndpoint() {
        return ResponseEntity.ok("This endpoint is hidden from Swagger documentation");
    }
}