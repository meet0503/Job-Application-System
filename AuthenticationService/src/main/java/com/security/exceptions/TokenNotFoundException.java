package com.security.exceptions;


@SuppressWarnings("serial")
public class TokenNotFoundException extends RuntimeException {
    public TokenNotFoundException(String message) {
        super(message);
    }
}