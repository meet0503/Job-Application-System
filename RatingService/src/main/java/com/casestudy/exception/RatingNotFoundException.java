package com.casestudy.exception;

@SuppressWarnings("serial")
public class RatingNotFoundException extends RuntimeException{
	public RatingNotFoundException(String message){
		super(message);
	}
}
