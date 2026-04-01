package com.vbforge.libraryapi.exception.custom;

import com.vbforge.libraryapi.exception.ApiException;
import org.springframework.http.HttpStatus;

public class BorrowRecordNotFoundException extends ApiException {
    public BorrowRecordNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}