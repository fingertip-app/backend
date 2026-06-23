package com.janginharou.global.exception;

public class ExternalServiceException extends ApplicationException {

    public ExternalServiceException(String message, String errorCode) {
        super(message, errorCode);
    }

    public ExternalServiceException(String message, Throwable cause, String errorCode) {
        super(message, cause, errorCode);
    }
}
