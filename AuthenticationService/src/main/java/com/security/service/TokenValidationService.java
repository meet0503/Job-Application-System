package com.security.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.security.utils.TokenValidationResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenValidationService {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public TokenValidationResponse validateToken(String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String username = jwtService.extractUsername(token);
                
                if (username != null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    boolean isValid = jwtService.isTokenValid(token, userDetails);
                    
                    if (isValid) {
                        String role = userDetails.getAuthorities().stream()
                                .findFirst()
                                .map(GrantedAuthority::getAuthority)
                                .orElse("USER");
                        
                        return new TokenValidationResponse(true, role);
                    }
                }
            }
            return new TokenValidationResponse(false, null);
        } 
        catch (Exception e) {
            return new TokenValidationResponse(false, null);
        }
    }
}