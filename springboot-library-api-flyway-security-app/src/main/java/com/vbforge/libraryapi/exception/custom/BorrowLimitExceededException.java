package com.vbforge.libraryapi.exception.custom;

import com.vbforge.libraryapi.exception.ApiException;
import org.springframework.http.HttpStatus;

public class BorrowLimitExceededException extends ApiException {
    public BorrowLimitExceededException(int maxLimit) {
        super(HttpStatus.BAD_REQUEST, "You cannot borrow more than " + maxLimit + " books.");
    }
}