package com.vbforge.wookie.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested resource is not found in the database.
 * Returns HTTP 404 Not Found status.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a new ResourceNotFoundException with default message.
     */
    public ResourceNotFoundException() {
        super("Resource not found");
    }

    /**
     * Constructs a new ResourceNotFoundException with custom message.
     * 
     * @param message Custom error message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new ResourceNotFoundException with custom message and cause.
     * 
     * @param message Custom error message
     * @param cause Original exception cause
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}