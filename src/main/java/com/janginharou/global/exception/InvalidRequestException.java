package com.janginharou.global.exception;

public class InvalidRequestException extends ApplicationException {

    public InvalidRequestException(String message) {
        super(message, "INVALID_REQUEST");
    }

    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause, "INVALID_REQUEST");
    }
}
