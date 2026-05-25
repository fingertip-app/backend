package com.janginharou.global.exception;

public class UnauthorizedException extends ApplicationException {

    public UnauthorizedException(String message) {
        super(message, "UNAUTHORIZED");
    }
}
