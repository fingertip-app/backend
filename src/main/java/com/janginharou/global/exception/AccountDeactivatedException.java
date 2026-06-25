package com.janginharou.global.exception;

public class AccountDeactivatedException extends UnauthorizedException {

    public AccountDeactivatedException() {
        super("This account has been deactivated");
    }
}
