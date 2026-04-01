package com.vbforge.libraryapi.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * this is custom exception class
 * */
@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    // Default to BAD_REQUEST (400) if no status is provided
    public ApiException(String message) {
        this(HttpStatus.BAD_REQUEST, message);
    }

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

}