package com.security.exceptions;


@SuppressWarnings("serial")
public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException(String message) {
        super(message);
    }
}
