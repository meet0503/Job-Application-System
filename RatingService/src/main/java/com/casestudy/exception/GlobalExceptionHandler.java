package com.casestudy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.casestudy.payload.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(RatingNotFoundException.class)
	public ResponseEntity<ApiResponse> handleRatingNotFoundException(RatingNotFoundException e){
		String message = e.getMessage();
		ApiResponse apiResponse = ApiResponse.builder()
			.message(message)
			.success(false)
			.build();
		
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
	}
}
