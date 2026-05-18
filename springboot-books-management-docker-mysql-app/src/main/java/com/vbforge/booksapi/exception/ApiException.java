package com.vbforge.booksapi.exception;

import lombok.Getter;

@Getter
public abstract class ApiException extends RuntimeException {
    
    private final int statusCode;
    private final String errorCode;
    
    protected ApiException(String message, int statusCode, String errorCode) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }
    
    protected ApiException(String message, int statusCode, String errorCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }
}