package com.vbforge.libraryapi.exception.custom;

import com.vbforge.libraryapi.exception.ApiException;
import org.springframework.http.HttpStatus;

public class BookNotFoundException extends ApiException {
    public BookNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "Book not found with ID: " + id);
    }
    public BookNotFoundException(String isbn) {
        super(HttpStatus.NOT_FOUND, "Book not found with ISBN: " + isbn);
    }
}