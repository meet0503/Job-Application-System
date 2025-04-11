package com.security.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.security.payload.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<ApiResponse> handleTokenNotFoundException(TokenNotFoundException ex) {
        ApiResponse apiResponse = ApiResponse.builder()
            .message(ex.getMessage())
            .success(false)
            .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ApiResponse> handleTokenExpiredException(TokenExpiredException ex) {
        ApiResponse apiResponse = ApiResponse.builder()
            .message(ex.getMessage())
            .success(false)
            .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }
}
