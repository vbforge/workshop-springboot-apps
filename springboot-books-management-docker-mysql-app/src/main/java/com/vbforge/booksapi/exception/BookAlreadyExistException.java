package com.vbforge.booksapi.exception;

public class BookAlreadyExistException extends ApiException {

    private static final String ERROR_CODE = "BOOK_002";
    private static final int STATUS_CODE = 409;

    public BookAlreadyExistException(String message) {
        super(message, STATUS_CODE, ERROR_CODE);
    }

    public BookAlreadyExistException(String message, Throwable cause) {
        super(message, STATUS_CODE, ERROR_CODE, cause);
    }

    public BookAlreadyExistException(Long id) {
        super("Book already exists with id: " + id, STATUS_CODE, ERROR_CODE);
    }
}

