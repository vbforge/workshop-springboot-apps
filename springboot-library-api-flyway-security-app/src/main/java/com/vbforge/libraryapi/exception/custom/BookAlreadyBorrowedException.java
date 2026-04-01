package com.vbforge.libraryapi.exception.custom;

import com.vbforge.libraryapi.exception.ApiException;
import org.springframework.http.HttpStatus;

public class BookAlreadyBorrowedException extends ApiException {
    public BookAlreadyBorrowedException(String bookTitle) {
        super(HttpStatus.BAD_REQUEST, "You have already borrowed: " + bookTitle);
    }
}