package com.vbforge.libraryapi.exception;

import com.vbforge.libraryapi.dto.response.ErrorResponse;
import com.vbforge.libraryapi.exception.custom.BookUnavailableException;
import com.vbforge.libraryapi.exception.custom.BorrowLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class UserControllerAdvice {
    @ExceptionHandler(BorrowLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleBorrowLimitExceeded(
            BorrowLimitExceededException ex, WebRequest request) {
        return GlobalExceptionHandler.buildResponse(
                HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(BookUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleBookUnavailable(
            BookUnavailableException ex, WebRequest request) {
        return GlobalExceptionHandler.buildResponse(
                HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }
}