package com.casestudy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;

@Configuration
public class FeignClientInterceptor {
	private static final Logger log = LoggerFactory.getLogger(FeignClientInterceptor.class);
	
	@Bean
	RequestInterceptor requestInterceptor() {
		// returns a request template object
		log.debug("Creating Feign RequestInterceptor bean.");
	    return template -> {
	        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
	        if (attributes != null) {
	            String authHeader = attributes.getRequest().getHeader("Authorization");
	            if (authHeader != null && !authHeader.isEmpty()) {
	                template.header("Authorization", authHeader);
	                log.debug("Propagating Authorization header to Feign request.");
	            }
	            else {
	            	log.debug("No Authorization header found in original request or header is empty. Not propagating.");
	            }
	        }
	        else {
	        	log.warn("RequestContextHolder returned null attributes. Cannot propagate Authorization header.");
	        }
	    };
	}

}
