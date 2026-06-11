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
    @Transactional // Đảm bảo nếu lỗi ở bất kỳ bước nào thì toàn bộ dữ liệu (User, KycProfile) sẽ rollback
    public ApiResponse<String> register(RegisterRequest request) {
        // 1. Kiểm tra tồn tại
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Tên đăng nhập đã tồn tại trong hệ thống");
        }
        if (kycProfileRepository.existsByIdNumber(request.getIdNumber())) {
            throw new BusinessException("ID Number already exists"); // Hoặc logic tương ứng của bạn
        }

        // 2. Tìm Role mặc định là CUSTOMER (Đảm bảo bạn đã insert ROLE_CUSTOMER vào DB trước đó)
        Role userRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new BusinessException("Lỗi cấu hình: Không tìm thấy Role CUSTOMER"));

        // 3. Khởi tạo User
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword())) // Mã hóa BCrypt
                .role(userRole)
                .isKyc(false) // Mới tạo chưa duyệt eKYC
                .build();

        // 4. Khởi tạo KycProfile với trạng thái PENDING theo chuẩn SRS
        KycProfile kycProfile = KycProfile.builder()
                .fullName(request.getFullName())
                .idNumber(request.getIdNumber())
                .status(KycStatus.PENDING)
                .user(user)
                .build();

        // Thiết lập quan hệ 2 chiều
        user.setKycProfile(kycProfile);

        // Lưu vào Database (Nhờ cấu hình CascadeType.ALL ở Entity User, KycProfile sẽ tự động được lưu theo)
        userRepository.save(user);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Đăng ký tài khoản thành công. Vui lòng chờ duyệt eKYC.")
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<TokenResponse> login(LoginRequest request) {
        // 1. Spring Security thực hiện đối chiếu Username/Password trong DB
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // 2. Ép kiểu để lấy CustomUserDetails
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 3. Tạo JWT Access Token
        String accessToken = jwtProvider.generateToken(userDetails);

        // 4. Lấy KycProfile và chạy cơ chế phòng thủ NullPointerException
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

        // 5 & 6. Xử lý Refresh Token: CẬP NHẬT ĐÈ THAY VÌ XÓA
        RefreshToken refreshToken = kycProfile.getRefreshToken();

        if (refreshToken == null) {
            // Nếu chưa có (đăng nhập lần đầu), tạo đối tượng mới
            refreshToken = RefreshToken.builder()
                    .kycProfile(kycProfile)
                    .build();
        }

        // Cập nhật giá trị Token và thời gian hết hạn mới
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshExpiration));

        // Lưu xuống DB (JPA sẽ tự động Update nếu đã tồn tại)
        refreshTokenRepository.save(refreshToken);

        // Đồng bộ lại vào đối tượng kycProfile đang ở trong RAM
        kycProfile.setRefreshToken(refreshToken);

        // 7. Đóng gói kết quả
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
        // 1. Tìm Refresh Token trong DB
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BusinessException("Refresh Token không hợp lệ hoặc không tồn tại"));

        // 2. Kiểm tra hạn sử dụng
        if (refreshToken.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenExpiredException("Refresh Token đã hết hạn. Vui lòng đăng nhập lại.");
        }

        // 3. Lấy thông tin User để sinh Access Token mới
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
        // Cắt bỏ chuỗi "Bearer " nếu có
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }

        // Đưa Access Token hiện tại vào Blacklist để Filter chặn các Request tiếp theo
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
        // 1. Tìm user theo Email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Email không tồn tại trong hệ thống"));

        // 2. Tự động sinh ra một mật khẩu ngẫu nhiên gồm 6 chữ số
        String newPassword = String.format("%06d", new java.util.Random().nextInt(999999));

        // 3. Mã hóa và lưu mật khẩu mới
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 4. Giả lập gửi Email thông báo (In ra Console để bạn dễ dàng test)
        System.out.println("==================================================");
        System.out.println("ĐANG GỬI EMAIL TỚI: " + user.getEmail());
        System.out.println("Mật khẩu mới của bạn là: " + newPassword);
        System.out.println("Vui lòng đăng nhập lại và đổi mật khẩu ngay lập tức!");
        System.out.println("==================================================");
    }
}