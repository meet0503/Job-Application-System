package com.security.service;


import java.time.Instant;
import java.util.UUID;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.security.entities.RefreshToken;
import com.security.entities.User;
import com.security.exceptions.TokenExpiredException;
import com.security.exceptions.TokenNotFoundException;
import com.security.properties.JwtProperties;
import com.security.repositories.RefreshTokenRepository;
import com.security.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final UserRepository userRepository;
    
    private final JwtProperties jwtProperties;
    

    public RefreshToken createRefreshToken(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with Username : " + username));

        RefreshToken refreshToken = user.getRefreshToken();

        if (refreshToken == null) {
            refreshToken = RefreshToken.builder()
                    .token(UUID.randomUUID().toString())
                    .expirationTime(Instant.now().plusMillis(jwtProperties.getRefreshTokenExpiration()))
                    .user(user)
                    .build();

            // 🔥 VERY IMPORTANT: Link it back to the user entity
            user.setRefreshToken(refreshToken);
        } else {
            // update old token
            refreshToken.setToken(UUID.randomUUID().toString());
            refreshToken.setExpirationTime(Instant.now().plusMillis(jwtProperties.getRefreshTokenExpiration()));
        }

        return refreshTokenRepository.save(refreshToken);
    }


    public RefreshToken verifyRefreshToken(String refreshToken) {
        RefreshToken refreshTokenOb = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new TokenNotFoundException("refresh token not exist"));

        if (refreshTokenOb.getExpirationTime().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(refreshTokenOb);
            throw new TokenExpiredException("Refresh Token expired");
        }

        return refreshTokenOb;
    }
}