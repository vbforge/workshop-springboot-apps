package com.vbforge.bookapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI Configuration
 * Provides API documentation at /swagger-ui.html
 */
@Configuration
public class SwaggerConfig {

    @Value("${spring.application.name:Book Management API}")
    private String applicationName;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(apiServers());
    }

    /**
     * API Information
     */
    private Info apiInfo() {
        return new Info()
                .title("📚 Book Management REST API")
                .version("1.0.0")
                .description("""
                        **RESTful API for managing books, authors, categories, and publishers.**
                        
                        This API provides comprehensive endpoints for:
                        - 📖 Book management (CRUD, search, filtering)
                        - ✍️ Author management
                        - 📂 Category management
                        - 🏢 Publisher management
                        
                        ### Features:
                        - Full CRUD operations
                        - Advanced search and filtering
                        - Pagination and sorting
                        - Stock management
                        - Validation and error handling
                        
                        ### Technologies:
                        - Java 17
                        - Spring Boot 3.2
                        - Spring Data JPA
                        - MySQL (dev) / H2 (test)
                        - MapStruct
                        - Swagger/OpenAPI 3
                        """)
                .contact(apiContact())
                .license(apiLicense());
    }

    /**
     * API Contact Information
     */
    private Contact apiContact() {
        return new Contact()
                .name("vbforge Java Developer")
                .email("contact@vbforge.com")
                .url("https://github.com/vbforge");
    }

    /**
     * API License
     */
    private License apiLicense() {
        return new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");
    }

    /**
     * API Servers
     */
    private List<Server> apiServers() {
        Server localServer = new Server()
                .url("http://localhost:8080")
                .description("Local Development Server");

        Server productionServer = new Server()
                .url("https://api.bookmanagement.com")
                .description("Production Server");

        return List.of(localServer, productionServer);
    }
}
