package com.casestudy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class JobServiceApplication {
	private static final Logger log = LoggerFactory.getLogger(JobServiceApplication.class);
	
	public static void main(String[] args) {
		SpringApplication.run(JobServiceApplication.class, args);
		log.info("JobServiceApplication started successfully.");
	}

}
