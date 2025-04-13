package com.casestudy.exception;

@SuppressWarnings("serial")
public class CompanyNotFoundException extends RuntimeException{
	public CompanyNotFoundException(String message){
		super(message);
	}
}
