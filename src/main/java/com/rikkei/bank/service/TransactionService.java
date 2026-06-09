package com.rikkei.bank.service;

import com.rikkei.bank.dto.ApiResponse;
import com.rikkei.bank.dto.TransactionResponse;
import com.rikkei.bank.dto.TransferRequest;
import org.springframework.data.domain.Page;

public interface TransactionService {
    // 1. Hàm chuyển tiền (Từ Phần 5)
    ApiResponse<TransactionResponse> transfer(TransferRequest request);

    // 2. Hàm xem lịch sử giao dịch (Từ Phần 7)
    ApiResponse<Page<TransactionResponse>> getTransactionHistory(String accountNumber, int page, int size);
}