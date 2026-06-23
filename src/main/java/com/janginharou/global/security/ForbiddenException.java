package com.janginharou.global.security;

import com.janginharou.global.exception.ApplicationException;

public class ForbiddenException extends ApplicationException {

    public ForbiddenException(String message) {
        super(message, "FORBIDDEN");
    }
}
