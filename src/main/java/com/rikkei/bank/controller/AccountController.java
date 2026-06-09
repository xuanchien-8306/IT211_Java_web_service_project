package com.rikkei.bank.controller;

import com.rikkei.bank.dto.CreateAccountRequest;
import com.rikkei.bank.dto.AccountResponse;
import com.rikkei.bank.dto.ApiResponse;
import com.rikkei.bank.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    // Yêu cầu quyền ROLE_CUSTOMER
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        return ResponseEntity.ok(accountService.createAccount(request));
    }

    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getMyAccounts() {
        return ResponseEntity.ok(accountService.getMyAccounts());
    }

    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountBalance(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccountBalance(accountNumber));
    }
}