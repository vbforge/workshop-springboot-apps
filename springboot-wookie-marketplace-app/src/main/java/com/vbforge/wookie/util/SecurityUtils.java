package com.vbforge.wookie.util;

import com.vbforge.wookie.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SecurityUtils {

    /**
     * Get the currently authenticated user
     */
    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        
        return null;
    }
    
    /**
     * Get current user's ID
     */
    public static Long getCurrentUserId() {
        User currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getUserId() : null;
    }
    
    /**
     * Check if current user is the owner of the resource or admin
     */
    public static boolean isOwnerOrAdmin(Long resourceUserId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        
        // Check if user is admin
        if (currentUser.getRole() == com.vbforge.wookie.entity.Roles.SUPER_ADMIN) {
            return true;
        }
        
        // Check if user is the owner
        return currentUser.getUserId().equals(resourceUserId);
    }
}