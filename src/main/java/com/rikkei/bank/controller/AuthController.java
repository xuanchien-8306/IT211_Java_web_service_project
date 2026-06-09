package com.rikkei.bank.controller;

import com.rikkei.bank.dto.LoginRequest;
import com.rikkei.bank.dto.RefreshTokenRequest;
import com.rikkei.bank.dto.RegisterRequest;
import com.rikkei.bank.dto.ApiResponse;
import com.rikkei.bank.dto.TokenResponse;
import com.rikkei.bank.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // API: POST /api/v1/auth/register
    // @Valid kích hoạt kiểm tra tính hợp lệ dữ liệu từ DTO (NotBlank, Size...)
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    // API: POST /api/v1/auth/login
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // API: POST /api/v1/auth/refresh
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    // API: POST /api/v1/auth/logout
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        // Lấy token từ Header "Authorization"
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<String>builder().success(false).message("Thiếu hoặc sai định dạng Token").build()
            );
        }

        return ResponseEntity.ok(authService.logout(authHeader));
    }
}