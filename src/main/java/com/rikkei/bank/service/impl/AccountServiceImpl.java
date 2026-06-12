package com.rikkei.bank.service.impl;

import com.rikkei.bank.dto.ChangePinRequest;
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
import com.rikkei.bank.util.CurrencyUtils;
import jakarta.transaction.Transactional;
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
    private final PasswordEncoder passwordEncoder;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Không tìm thấy thông tin người dùng xác thực"));
    }

    @Override
    public ApiResponse<AccountResponse> createAccount(CreateAccountRequest request) {
        User user = getCurrentUser();

        if (!user.getIsKyc()) {
            throw new BusinessException("Bạn cần hoàn tất định danh eKYC để mở tài khoản");
        }

        String newAccountNumber = String.valueOf(100000000L + new Random().nextInt(900000000));

        Account account = Account.builder()
                .accountNumber(newAccountNumber)
                .balance(BigDecimal.ZERO)
                .pin(passwordEncoder.encode(request.getPin()))
                .status("ACTIVE")
                .user(user)
                .build();

        accountRepository.save(account);

        return ApiResponse.<AccountResponse>builder()
                .success(true)
                .message("Tạo tài khoản thành công")
                .data(AccountResponse.builder()
                        .id(account.getId())
                        .accountNumber(account.getAccountNumber())
                        .balance(account.getBalance())
                        .status(account.getStatus())
                        .formattedBalance(CurrencyUtils.formatVND(account.getBalance()))
                        .build())
                .build();
    }

    @Override
    public ApiResponse<AccountResponse> getAccountBalance(String accountNumber) {
        User user = getCurrentUser();

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Tài khoản không tồn tại"));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Bạn không có quyền truy cập tài khoản này");
        }

        AccountResponse response = AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .status(account.getStatus())
                .formattedBalance(CurrencyUtils.formatVND(account.getBalance()))
                .build();

        return ApiResponse.<AccountResponse>builder()
                .success(true)
                .message("Truy vấn số dư thành công")
                .data(response)
                .build();
    }

    @Override
    public ApiResponse<List<AccountResponse>> getMyAccounts() {
        User user = getCurrentUser();
        List<AccountResponse> accounts = user.getAccounts().stream()
                .map(a -> AccountResponse.builder()
                        .id(a.getId())
                        .accountNumber(a.getAccountNumber())
                        .balance(a.getBalance())
                        .status(a.getStatus())
                        .formattedBalance(CurrencyUtils.formatVND(a.getBalance()))
                        .build())
                .collect(Collectors.toList());

        return ApiResponse.<List<AccountResponse>>builder()
                .success(true)
                .message("Lấy danh sách tài khoản thành công")
                .data(accounts)
                .build();
    }

    @Override
    @Transactional
    public void changePin(String accountNumber, ChangePinRequest request) {
        if (!request.getNewPin().equals(request.getConfirmPin())) {
            throw new BusinessException("Mã PIN xác nhận không khớp với mã PIN mới");
        }

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BusinessException("Tài khoản không tồn tại"));

        if (!account.getUser().getUsername().equals(currentUsername)) {
            throw new BusinessException("Bạn không có quyền đổi mã PIN của tài khoản này");
        }

        if (!passwordEncoder.matches(request.getOldPin(), account.getPin())) {
            throw new BusinessException("Mã PIN cũ không chính xác");
        }

        account.setPin(passwordEncoder.encode(request.getNewPin()));
        accountRepository.save(account);
    }
}