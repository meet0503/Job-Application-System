package com.casestudy.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.casestudy.payload.ApiResponse;

import feign.FeignException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	
    @ExceptionHandler(JobNotFoundException.class)
    public ResponseEntity<ApiResponse> handleJobNotFoundException(JobNotFoundException e) {
        String message = e.getMessage();
        log.warn("Handling JobNotFoundException: {}", message);
        ApiResponse apiResponse = ApiResponse.builder()
                .message(message)
                .success(false)
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
    }

    
    @ExceptionHandler(CompanyNotFoundException.class)
	public ResponseEntity<ApiResponse> handleCompanyNotFoundException(CompanyNotFoundException e){
		String message = e.getMessage();
		log.warn("Handling CompanyNotFoundException: {}", message);
		ApiResponse apiResponse = ApiResponse.builder()
			.message(message)
			.success(false)
			.build();
		
		return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
	}
    
    
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiResponse> handleFeignException(FeignException e) {
        String message = "Error from external service: " + e.getMessage();
        log.error("Handling FeignException: {}", message, e);
        ApiResponse apiResponse = ApiResponse.builder()
                .message(message)
                .success(false)
                .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(apiResponse);
    }
}