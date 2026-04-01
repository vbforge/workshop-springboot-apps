package com.vbforge.libraryapi.exception.custom;

import com.vbforge.libraryapi.exception.ApiException;

public class UserAlreadyExistException extends ApiException {
    public UserAlreadyExistException(String message) {
        super(message); // Defaults to HttpStatus.BAD_REQUEST (400)
    }
}
