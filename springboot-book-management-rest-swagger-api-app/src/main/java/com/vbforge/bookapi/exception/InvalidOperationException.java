package com.vbforge.bookapi.exception;

/**
 * Exception thrown when an operation cannot be performed
 * Results in HTTP 400 Bad Request status
 */
public class InvalidOperationException extends RuntimeException {
    
    public InvalidOperationException(String message) {
        super(message);
    }
    
    public InvalidOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}