package com.rikkei.bank.service;

import com.rikkei.bank.dto.KycApproveRequest;
import com.rikkei.bank.dto.ApiResponse;

public interface AdminService {
    ApiResponse<String> approveKyc(Long userId, KycApproveRequest request);
}