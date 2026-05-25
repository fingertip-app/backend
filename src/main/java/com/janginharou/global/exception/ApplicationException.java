package com.janginharou.global.exception;

public abstract class ApplicationException extends RuntimeException {

    private final String errorCode;

    public ApplicationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ApplicationException(String message, Throwable cause, String errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
