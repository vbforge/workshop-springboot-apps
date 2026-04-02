package com.vbforge.wookie.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a user tries to access a resource they don't have permission for.
 * Returns HTTP 403 Forbidden status.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class PermissionDeniedException extends RuntimeException {

    /**
     * Constructs a new PermissionDeniedException with default message.
     */
    public PermissionDeniedException() {
        super("Permission denied");
    }

    /**
     * Constructs a new PermissionDeniedException with custom message.
     * 
     * @param message Custom error message
     */
    public PermissionDeniedException(String message) {
        super(message);
    }

    /**
     * Constructs a new PermissionDeniedException with custom message and cause.
     * 
     * @param message Custom error message
     * @param cause Original exception cause
     */
    public PermissionDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}