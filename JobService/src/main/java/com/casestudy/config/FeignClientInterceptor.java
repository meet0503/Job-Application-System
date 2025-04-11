package com.casestudy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;

@Configuration
public class FeignClientInterceptor {

	@Bean
	RequestInterceptor requestInterceptor() {
		// returns a request template object
	    return template -> {
	        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
	        if (attributes != null) {
	            String authHeader = attributes.getRequest().getHeader("Authorization");
	            if (authHeader != null && !authHeader.isEmpty()) {
	                template.header("Authorization", authHeader);
	            }
	        }
	    };
	}

}
