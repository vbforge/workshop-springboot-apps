package com.vbforge.libraryapi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.librarian")
@Getter
@Setter
public class LibrarianProperties {
    private String username;
    private String email;
    private String password;
}