package com.rikkei.bank.service.impl;

import com.rikkei.bank.dto.TransferRequest;
import com.rikkei.bank.dto.ApiResponse;
import com.rikkei.bank.dto.TransactionResponse;
import com.rikkei.bank.entity.Account;
import com.rikkei.bank.entity.Transaction;
import com.rikkei.bank.entity.User;
import com.rikkei.bank.exception.AccountNotFoundException;
import com.rikkei.bank.exception.BusinessException;
import com.rikkei.bank.exception.InsufficientBalanceException;
import com.rikkei.bank.exception.InvalidPinException;
import com.rikkei.bank.repository.AccountRepository;
import com.rikkei.bank.repository.TransactionRepository;
import com.rikkei.bank.repository.UserRepository;
import com.rikkei.bank.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<TransactionResponse> transfer(TransferRequest request) {
        if (request.getFromAccount().equals(request.getToAccount())) {
            throw new BusinessException("Không thể chuyển tiền cho chính tài khoản của bạn");
        }

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new BusinessException("Lỗi xác thực người dùng"));

        Account checkFromAcc = accountRepository.findByAccountNumber(request.getFromAccount())
                .orElseThrow(() -> new AccountNotFoundException("Tài khoản nguồn không tồn tại"));

        if (!checkFromAcc.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("Bạn không phải chủ sở hữu tài khoản nguồn này");
        }

        if (!passwordEncoder.matches(request.getPin(), checkFromAcc.getPin())) {
            throw new InvalidPinException("Mã PIN không chính xác");
        }

        // Chống Deadlock: Khóa tài khoản có STT nhỏ hơn trước
        String firstLock = request.getFromAccount().compareTo(request.getToAccount()) < 0
                ? request.getFromAccount() : request.getToAccount();
        String secondLock = request.getFromAccount().compareTo(request.getToAccount()) < 0
                ? request.getToAccount() : request.getFromAccount();

        accountRepository.findByAccountNumberForUpdate(firstLock)
                .orElseThrow(() -> new AccountNotFoundException("Tài khoản không tồn tại"));
        accountRepository.findByAccountNumberForUpdate(secondLock)
                .orElseThrow(() -> new AccountNotFoundException("Tài khoản không tồn tại"));

        Account fromAccount = accountRepository.findByAccountNumber(request.getFromAccount()).get();
        Account toAccount = accountRepository.findByAccountNumber(request.getToAccount()).get();

        if (!"ACTIVE".equals(fromAccount.getStatus()) || !"ACTIVE".equals(toAccount.getStatus())) {
            throw new BusinessException("Một trong hai tài khoản đang bị khóa");
        }

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Số dư không đủ để thực hiện giao dịch");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        Transaction transaction = Transaction.builder()
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(request.getAmount())
                .content(request.getDescription())
                .timestamp(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);

        TransactionResponse responseDto = TransactionResponse.builder()
                .id(transaction.getId())
                .fromAccount(fromAccount.getAccountNumber())
                .toAccount(toAccount.getAccountNumber())
                .amount(transaction.getAmount())
                .content(transaction.getContent())
                .timestamp(transaction.getTimestamp())
                .type("DEBIT")
                .build();

        return ApiResponse.<TransactionResponse>builder()
                .success(true)
                .message("Chuyển khoản thành công")
                .data(responseDto)
                .build();
    }

    @Override
    public ApiResponse<Page<TransactionResponse>> getTransactionHistory(String accountNumber, int page, int size) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new BusinessException("Lỗi xác thực"));

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Tài khoản không tồn tại"));

        if (!account.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("Bạn không có quyền truy cập sao kê tài khoản này");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactionPage = transactionRepository.findTransactionHistory(accountNumber, pageable);

        Page<TransactionResponse> responsePage = transactionPage.map(tx -> {
            String type = tx.getFromAccount().getAccountNumber().equals(accountNumber) ? "DEBIT" : "CREDIT";
            return TransactionResponse.builder()
                    .id(tx.getId())
                    .fromAccount(tx.getFromAccount().getAccountNumber())
                    .toAccount(tx.getToAccount().getAccountNumber())
                    .amount(tx.getAmount())
                    .content(tx.getContent())
                    .timestamp(tx.getTimestamp())
                    .type(type)
                    .build();
        });

        return ApiResponse.<Page<TransactionResponse>>builder()
                .success(true)
                .message("Lấy lịch sử giao dịch thành công")
                .data(responsePage)
                .build();
    }
}