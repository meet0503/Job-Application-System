package com.casestudy.security;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.casestudy.entities.external.TokenValidationResponse;
import com.casestudy.feign.AuthServiceClient;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	
	private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
	
	// Feign client for making HTTP calls to the Authentication Service
    private final AuthServiceClient authServiceClient;

    public JwtAuthenticationFilter(AuthServiceClient authServiceClient) {
        this.authServiceClient = authServiceClient;
    }
    
 
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, 
            HttpServletResponse response, 
            FilterChain filterChain) throws ServletException, IOException {
        
    	log.debug("Processing request to: {}", request.getRequestURI());
    	
        // Extract JWT token from Authorization header
        final String authHeader = request.getHeader("Authorization");
        
        // Check if Authorization header exists and has the correct format
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        	
        	log.warn("Request missing Authorization header or incorrect format: {}", request.getRequestURI());
        	
            // Return 401 Unauthorized if the header is missing or malformed
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing Authorization header");
            return;
        }
        
        try {
            // Call external Authentication Service to validate the token
            TokenValidationResponse validationResponse = authServiceClient.validateToken(authHeader);
            
            // Process the token validation response
            if (validationResponse.isValid()) {
            	log.info("Token validated successfully for role: {} accessing: {}", 
                        validationResponse.getRole(), request.getRequestURI());
            	
                // Create authority based on the role returned by the AuthService
                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority(validationResponse.getRole())
                );
                
                // Create authentication token with granted authorities
                // Note: Principal and credentials are set to null as they're managed by the AuthService
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(null, null, authorities);
                    
                // Set authentication in the Security Context
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                // Continue with the filter chain if authentication is successful
                filterChain.doFilter(request, response);
            } else {
            	log.warn("Invalid token received for request to: {}", request.getRequestURI());
            	
                // Return 401 Unauthorized if the token is invalid or expired
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid or expired token");
            }
        } catch (Exception e) {
            // Handle any exceptions that occur during token validation
        	log.error("Error validating token for request to {}: {}", request.getRequestURI(), e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Error validating token: " + e.getMessage());
        }
    }
}