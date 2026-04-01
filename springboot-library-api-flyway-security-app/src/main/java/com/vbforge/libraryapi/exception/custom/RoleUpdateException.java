package com.vbforge.libraryapi.exception.custom;

import com.vbforge.libraryapi.exception.ApiException;
import org.springframework.http.HttpStatus;

public class RoleUpdateException extends ApiException {
    public RoleUpdateException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}