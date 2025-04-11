package com.casestudy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.casestudy.payload.ApiResponse;

import feign.FeignException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(JobNotFoundException.class)
    public ResponseEntity<ApiResponse> handleJobNotFoundException(JobNotFoundException e) {
        String message = e.getMessage();
        ApiResponse apiResponse = ApiResponse.builder()
                .message(message)
                .success(false)
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiResponse> handleFeignException(FeignException e) {
        String message = "Error from external service: " + e.getMessage();
        ApiResponse apiResponse = ApiResponse.builder()
                .message(message)
                .success(false)
                .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(apiResponse);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        String message = "Invalid input: " + e.getMessage();
        ApiResponse apiResponse = ApiResponse.builder()
                .message(message)
                .success(false)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
    }
}