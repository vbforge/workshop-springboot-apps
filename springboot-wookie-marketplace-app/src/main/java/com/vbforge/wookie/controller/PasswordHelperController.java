package com.vbforge.wookie.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/helper")
@RequiredArgsConstructor
public class PasswordHelperController {

    private final PasswordEncoder passwordEncoder;

    @PostMapping("/encode")
    public Map<String, String> encodePassword(@RequestParam String password) {
        String encoded = passwordEncoder.encode(password);
        
        Map<String, String> response = new HashMap<>();
        response.put("rawPassword", password);
        response.put("encodedPassword", encoded);
        response.put("verification", String.valueOf(passwordEncoder.matches(password, encoded)));
        
        return response;
    }
}