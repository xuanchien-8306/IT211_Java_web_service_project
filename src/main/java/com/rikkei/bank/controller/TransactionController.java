package com.rikkei.bank.controller;

import com.rikkei.bank.dto.TransferRequest;
import com.rikkei.bank.dto.ApiResponse;
import com.rikkei.bank.dto.TransactionResponse;
import com.rikkei.bank.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // API thực hiện chuyển tiền (Step 6, 8, 9 trong kịch bản Postman của bạn)
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(@Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(transactionService.transfer(request));
    }

    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @GetMapping("/history/{accountNumber}")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getHistory(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(transactionService.getTransactionHistory(accountNumber, page, size));
    }
}