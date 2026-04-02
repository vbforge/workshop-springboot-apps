package com.vbforge.wookie.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Set your desired passwords here
        String adminPassword = "admin123";      // Change to your desired admin password
        String userPassword = "test123";        // Change to your desired test user password
        String darthPassword = "darth123";        // Change to your desired test user password

        // Generate hashes
        String adminHash = encoder.encode(adminPassword);
        String userHash = encoder.encode(userPassword);
        String darthHash = encoder.encode(darthPassword);

        System.out.println("=================================");
        System.out.println("SUPER_ADMIN (Lohgarra)");
        System.out.println("Password: " + adminPassword);
        System.out.println("BCrypt Hash: " + adminHash);
        System.out.println();
        System.out.println("Regular USER (TestAuthor)");
        System.out.println("Password: " + userPassword);
        System.out.println("BCrypt Hash: " + userHash);
        System.out.println();
        System.out.println("Regular RESTRICTED_USER (DarthVader)");
        System.out.println("Password: " + darthPassword);
        System.out.println("BCrypt Hash: " + darthHash);
        System.out.println("=================================");
        
        // Verify (optional)
        System.out.println("\nVerification:");
        System.out.println("Admin password matches: " + encoder.matches(adminPassword, adminHash));
        System.out.println("User password matches: " + encoder.matches(userPassword, userHash));
        System.out.println("Darth password matches: " + encoder.matches(darthPassword, darthHash));
    }
}