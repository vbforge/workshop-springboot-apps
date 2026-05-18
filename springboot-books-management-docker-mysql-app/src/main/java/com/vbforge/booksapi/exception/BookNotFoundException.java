package com.vbforge.booksapi.exception;

public class BookNotFoundException extends ApiException {

    private static final String ERROR_CODE = "BOOK_001";
    private static final int STATUS_CODE = 404;

    public BookNotFoundException(String message) {
        super(message, STATUS_CODE, ERROR_CODE);
    }

    public BookNotFoundException(String message, Throwable cause) {
        super(message, STATUS_CODE, ERROR_CODE, cause);
    }

    public BookNotFoundException(Long id) {
        super("Book not found with id: " + id, STATUS_CODE, ERROR_CODE);
    }
}

