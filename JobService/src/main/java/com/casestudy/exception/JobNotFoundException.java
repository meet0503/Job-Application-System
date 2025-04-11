package com.casestudy.exception;


@SuppressWarnings("serial")
public class JobNotFoundException extends RuntimeException{
	public JobNotFoundException(String message){
		super(message);
	}
}
