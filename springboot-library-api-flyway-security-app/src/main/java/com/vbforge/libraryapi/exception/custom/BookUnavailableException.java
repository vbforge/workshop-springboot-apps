package com.vbforge.libraryapi.exception.custom;

import com.vbforge.libraryapi.exception.ApiException;
import org.springframework.http.HttpStatus;

public class BookUnavailableException extends ApiException {
    public BookUnavailableException(String title) {
        super(HttpStatus.BAD_REQUEST, "Book '" + title + "' is not available.");
    }
}