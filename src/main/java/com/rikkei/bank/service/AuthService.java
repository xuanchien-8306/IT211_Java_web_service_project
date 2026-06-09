package com.rikkei.bank.service;

import com.rikkei.bank.dto.LoginRequest;
import com.rikkei.bank.dto.RefreshTokenRequest;
import com.rikkei.bank.dto.RegisterRequest;
import com.rikkei.bank.dto.ApiResponse;
import com.rikkei.bank.dto.TokenResponse;

public interface AuthService {
    ApiResponse<String> register(RegisterRequest request);
    ApiResponse<TokenResponse> login(LoginRequest request);
    ApiResponse<TokenResponse> refreshToken(RefreshTokenRequest request);
    ApiResponse<String> logout(String accessToken);
}