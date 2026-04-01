package com.vbforge.libraryapi.exception.custom;

import com.vbforge.libraryapi.exception.ApiException;
import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiException {
    public BadRequestException(String message) {
        super(message); // Defaults to HttpStatus.BAD_REQUEST (400)
    }
}