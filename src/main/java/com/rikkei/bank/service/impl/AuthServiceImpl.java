package com.rikkei.bank.service.impl;

import com.rikkei.bank.dto.*;
import com.rikkei.bank.entity.*;
import com.rikkei.bank.exception.BusinessException;
import com.rikkei.bank.exception.TokenExpiredException;
import com.rikkei.bank.repository.KycProfileRepository;
import com.rikkei.bank.repository.RefreshTokenRepository;
import com.rikkei.bank.repository.RoleRepository;
import com.rikkei.bank.repository.TokenBlacklistRepository;
import com.rikkei.bank.repository.UserRepository;
import com.rikkei.bank.security.CustomUserDetails;
import com.rikkei.bank.security.JwtProvider;
import com.rikkei.bank.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final KycProfileRepository kycProfileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Override
    public ApiResponse<String> register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Tên đăng nhập đã tồn tại trong hệ thống");
        }
        if (kycProfileRepository.existsByIdNumber(request.getIdNumber())) {
            throw new BusinessException("ID Number already exists");
        }

        Role userRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new BusinessException("Lỗi cấu hình: Không tìm thấy Role CUSTOMER"));

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(userRole)
                .isKyc(false)
                .build();

        KycProfile kycProfile = KycProfile.builder()
                .fullName(request.getFullName())
                .idNumber(request.getIdNumber())
                .status(KycStatus.PENDING)
                .user(user)
                .build();

        user.setKycProfile(kycProfile);

        userRepository.save(user);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Đăng ký tài khoản thành công. Vui lòng chờ duyệt eKYC.")
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<TokenResponse> login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = jwtProvider.generateToken(userDetails);

        KycProfile kycProfile = userDetails.getUser().getKycProfile();

        if (kycProfile == null) {
            kycProfile = KycProfile.builder()
                    .fullName("System Administrator")
                    .idNumber("ADMIN-" + System.currentTimeMillis())
                    .status(KycStatus.CONFIRM)
                    .user(userDetails.getUser())
                    .build();
            kycProfile = kycProfileRepository.save(kycProfile);
            userDetails.getUser().setKycProfile(kycProfile);
        }

        RefreshToken refreshToken = kycProfile.getRefreshToken();

        if (refreshToken == null) {
            refreshToken = RefreshToken.builder()
                    .kycProfile(kycProfile)
                    .build();
        }

        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshExpiration));

        refreshTokenRepository.save(refreshToken);

        kycProfile.setRefreshToken(refreshToken);

        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .build();

        return ApiResponse.<TokenResponse>builder()
                .success(true)
                .message("Đăng nhập thành công")
                .data(tokenResponse)
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<TokenResponse> refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BusinessException("Refresh Token không hợp lệ hoặc không tồn tại"));

        if (refreshToken.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenExpiredException("Refresh Token đã hết hạn. Vui lòng đăng nhập lại.");
        }

        User user = refreshToken.getKycProfile().getUser();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String newAccessToken = jwtProvider.generateToken(userDetails);

        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .build();

        return ApiResponse.<TokenResponse>builder()
                .success(true)
                .message("Làm mới Access Token thành công")
                .data(tokenResponse)
                .build();
    }

    @Override
    public ApiResponse<String> logout(String accessToken) {
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }

        TokenBlacklist blacklist = TokenBlacklist.builder()
                .token(accessToken)
                .revokedAt(LocalDateTime.now())
                .build();

        tokenBlacklistRepository.save(blacklist);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Đăng xuất thành công")
                .build();
    }

    @Override
    @Transactional
    public void resetPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Email không tồn tại trong hệ thống"));

        String newPassword = String.format("%06d", new java.util.Random().nextInt(999999));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        System.out.println("==================================================");
        System.out.println("ĐANG GỬI EMAIL TỚI: " + user.getEmail());
        System.out.println("Mật khẩu mới của bạn là: " + newPassword);
        System.out.println("Vui lòng đăng nhập lại và đổi mật khẩu ngay lập tức!");
        System.out.println("==================================================");
    }
}