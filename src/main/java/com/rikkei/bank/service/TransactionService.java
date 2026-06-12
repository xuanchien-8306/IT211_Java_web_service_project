package com.rikkei.bank.service;

import com.rikkei.bank.dto.ApiResponse;
import com.rikkei.bank.dto.TransactionResponse;
import com.rikkei.bank.dto.TransferRequest;
import org.springframework.data.domain.Page;

public interface TransactionService {
    ApiResponse<TransactionResponse> transfer(TransferRequest request);

    ApiResponse<Page<TransactionResponse>> getTransactionHistory(String accountNumber, int page, int size);
}