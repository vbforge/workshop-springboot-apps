package com.vbforge.wookie.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestAuthController {

    @GetMapping("/auth-info")
    public Map<String, Object> getAuthInfo(Principal principal, Authentication authentication) {
        Map<String, Object> info = new HashMap<>();
        
        if (principal != null) {
            info.put("principalName", principal.getName());
        }
        
        if (authentication != null) {
            info.put("authenticated", authentication.isAuthenticated());
            info.put("name", authentication.getName());
            info.put("authorities", authentication.getAuthorities());
            
            if (authentication.getPrincipal() != null) {
                Object principal_obj = authentication.getPrincipal();
                info.put("principalClass", principal_obj.getClass().getName());
                
                // Try to get userId if available
                try {
                    java.lang.reflect.Method method = principal_obj.getClass().getMethod("getUserId");
                    Long userId = (Long) method.invoke(principal_obj);
                    info.put("userId", userId);
                } catch (Exception e) {
                    info.put("userId", "Not available");
                }
            }
        }
        
        return info;
    }
}