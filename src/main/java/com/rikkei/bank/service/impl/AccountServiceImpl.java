package com.rikkei.bank.service.impl;

import com.rikkei.bank.dto.CreateAccountRequest;
import com.rikkei.bank.dto.AccountResponse;
import com.rikkei.bank.dto.ApiResponse;
import com.rikkei.bank.entity.Account;
import com.rikkei.bank.entity.User;
import com.rikkei.bank.exception.AccountNotFoundException;
import com.rikkei.bank.exception.BusinessException;
import com.rikkei.bank.repository.AccountRepository;
import com.rikkei.bank.repository.UserRepository;
import com.rikkei.bank.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Dùng BCrypt để mã hóa mã PIN

    // Lấy User đang đăng nhập từ Security Context (Token)
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Không tìm thấy thông tin người dùng xác thực"));
    }

    @Override
    public ApiResponse<AccountResponse> createAccount(CreateAccountRequest request) {
        User user = getCurrentUser();

        // Kiểm tra xem đã KYC chưa (Theo SRS, phải KYC mới được mở tài khoản giao dịch)
        if (!user.getIsKyc()) {
            throw new BusinessException("Bạn cần hoàn tất định danh eKYC để mở tài khoản");
        }

        // Sinh ngẫu nhiên số tài khoản 9 số (VD: 100000001)
        String newAccountNumber = String.valueOf(100000000L + new Random().nextInt(900000000));

        Account account = Account.builder()
                .accountNumber(newAccountNumber)
                .balance(BigDecimal.ZERO)
                .pin(passwordEncoder.encode(request.getPin())) // Mã hóa PIN theo chuẩn SRS
                .status("ACTIVE")
                .user(user)
                .build();

        accountRepository.save(account);

        return ApiResponse.<AccountResponse>builder()
                .success(true)
                .message("Tạo tài khoản thành công")
                .data(new AccountResponse(account.getId(), account.getAccountNumber(), account.getBalance(), account.getStatus()))
                .build();
    }

    @Override
    public ApiResponse<AccountResponse> getAccountBalance(String accountNumber) {
        User user = getCurrentUser();

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Tài khoản không tồn tại"));

        // Bảo mật: Khách hàng chỉ được xem số dư tài khoản của chính mình
        if (!account.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Bạn không có quyền truy cập tài khoản này");
        }

        return ApiResponse.<AccountResponse>builder()
                .success(true)
                .message("Truy vấn số dư thành công")
                .data(new AccountResponse(account.getId(), account.getAccountNumber(), account.getBalance(), account.getStatus()))
                .build();
    }

    @Override
    public ApiResponse<List<AccountResponse>> getMyAccounts() {
        User user = getCurrentUser();
        List<AccountResponse> accounts = user.getAccounts().stream()
                .map(a -> new AccountResponse(a.getId(), a.getAccountNumber(), a.getBalance(), a.getStatus()))
                .collect(Collectors.toList());

        return ApiResponse.<List<AccountResponse>>builder()
                .success(true)
                .message("Lấy danh sách tài khoản thành công")
                .data(accounts)
                .build();
    }
}