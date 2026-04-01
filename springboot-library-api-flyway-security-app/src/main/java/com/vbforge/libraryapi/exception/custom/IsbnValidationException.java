package com.vbforge.libraryapi.exception.custom;

import com.vbforge.libraryapi.exception.ApiException;
import org.springframework.http.HttpStatus;

public class IsbnValidationException extends ApiException {
    public IsbnValidationException(String isbn) {
        super(HttpStatus.BAD_REQUEST, "Invalid ISBN: " + isbn + ". Must be 13 digits.");
    }
}