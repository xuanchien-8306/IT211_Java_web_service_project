package com.rikkei.bank.service;

import com.rikkei.bank.dto.*;

public interface AuthService {
    ApiResponse<RegisterResponse> register(RegisterRequest request);
    ApiResponse<TokenResponse> login(LoginRequest request);
    ApiResponse<TokenResponse> refreshToken(RefreshTokenRequest request);
    ApiResponse<String> logout(String accessToken);
    void resetPassword(ForgotPasswordRequest request);
}