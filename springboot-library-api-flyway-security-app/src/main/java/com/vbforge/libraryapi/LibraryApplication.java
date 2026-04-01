package com.vbforge.libraryapi;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.vbforge.libraryapi.config.LibrarianProperties;

import java.nio.file.Paths;

@SpringBootApplication
@EnableConfigurationProperties(LibrarianProperties.class)
public class LibraryApplication {

    public static void main(String[] args) {

            Dotenv dotenv = Dotenv.load();
            System.setProperty("DB_URL", dotenv.get("DB_URL"));
            System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
            System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
            System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
            System.setProperty("JWT_EXPIRATION", dotenv.get("JWT_EXPIRATION"));



        SpringApplication.run(LibraryApplication.class, args);
    }
}