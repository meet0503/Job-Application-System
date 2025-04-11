package com.casestudy.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.casestudy.entities.external.TokenValidationResponse;

@FeignClient(name = "AUTHENTICATIONSERVICE")
public interface AuthServiceClient {
    @GetMapping("/api/v1/auth/validate")
    TokenValidationResponse validateToken(@RequestHeader("Authorization") String token);
}
