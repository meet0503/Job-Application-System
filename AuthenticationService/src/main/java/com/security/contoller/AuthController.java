package com.security.contoller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.security.entities.RefreshToken;
import com.security.entities.User;
import com.security.service.AuthService;
import com.security.service.JwtService;
import com.security.service.RefreshTokenService;
import com.security.service.TokenValidationService;
import com.security.utils.AuthResponse;
import com.security.utils.LoginRequest;
import com.security.utils.RefreshTokenRequest;
import com.security.utils.RegisterRequest;
import com.security.utils.TokenValidationResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/")
public class AuthController {

    private final AuthService authService;

    private final RefreshTokenService refreshTokenService;

    private final JwtService jwtService;
    
    private final TokenValidationService tokenValidationService;

    // endPoint to register new user
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    // endPoint to authenticate user for login
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request) {

        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(request.getRefreshToken());
        User user = refreshToken.getUser();

        String token = this.jwtService.generateToken(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .refreshToken(refreshToken.getToken())
                .token(token)
                .build());
    }
    

    @GetMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(tokenValidationService.validateToken(authHeader));
    }
}
