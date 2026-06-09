package com.rikkei.bank.controller;

import com.rikkei.bank.dto.KycApproveRequest;
import com.rikkei.bank.dto.ApiResponse;
import com.rikkei.bank.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // Yêu cầu quyền ROLE_ADMIN
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/kyc/approve/{userId}")
    public ResponseEntity<ApiResponse<String>> approveKyc(
            @PathVariable Long userId,
            @Valid @RequestBody KycApproveRequest request) {
        return ResponseEntity.ok(adminService.approveKyc(userId, request));
    }
}