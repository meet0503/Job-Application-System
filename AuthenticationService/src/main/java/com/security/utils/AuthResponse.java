package com.security.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class AuthResponse {

    private String token;
    private String refreshToken;
}
