package com.vbforge.libraryapi.exception.custom;

import com.vbforge.libraryapi.exception.ApiException;
import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ApiException {
    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}