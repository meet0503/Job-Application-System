package com.casestudy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.casestudy.payload.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

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
}
