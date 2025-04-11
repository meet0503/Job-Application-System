package com.security.service;


import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.security.entities.User;
import com.security.entities.UserRole;
import com.security.repositories.UserRepository;
import com.security.utils.AuthResponse;
import com.security.utils.LoginRequest;
import com.security.utils.RegisterRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	
	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;
	private final JwtService jwtService;
	private final RefreshTokenService refreshTokenService;
	private final AuthenticationManager authenticationManager;
	
	 public AuthResponse register(RegisterRequest request) {
	        var user = User.builder()
	                .username(request.getUsername())
	                .password(passwordEncoder.encode(request.getPassword()))
	                .role(UserRole.USER)
	                .build();
	        User savedUser = userRepository.save(user);
	        var jwt = jwtService.generateToken(savedUser);
	        var refreshToken = refreshTokenService.createRefreshToken(savedUser.getUsername());
	        return AuthResponse.builder()
	                .token(jwt)
	                .refreshToken(refreshToken.getToken())
	                .build();
	                
	    }
	 
	 public AuthResponse authenticate(LoginRequest request) {
	        authenticationManager.authenticate(
	                new UsernamePasswordAuthenticationToken(
	                        request.getUsername(),
	                        request.getPassword()
	                )
	        );

	        var user = userRepository.findByUsername(request.getUsername())
	                .orElseThrow();
	        var jwt = jwtService.generateToken(user);
	        var refreshToken = refreshTokenService.createRefreshToken(user.getUsername());
	        return AuthResponse.builder()
	                .token(jwt)
	                .refreshToken(refreshToken.getToken())
	                .build();
	    }

}
