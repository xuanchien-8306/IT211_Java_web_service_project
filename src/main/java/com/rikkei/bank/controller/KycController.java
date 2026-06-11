package com.rikkei.bank.controller;

import com.rikkei.bank.dto.ApiResponse;
import com.rikkei.bank.dto.KycApproveRequest;
import com.rikkei.bank.entity.KycProfile;
import com.rikkei.bank.service.KycService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Bắt buộc import
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/kyc")
@RequiredArgsConstructor
public class KycController {

    private final KycService kycService;

    // API Upload: Public cho Customer đã đăng nhập
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<KycProfile>> uploadKycDocument(
            @RequestParam("front") MultipartFile front,
            @RequestParam("back") MultipartFile back,
            @RequestParam("portrait") MultipartFile portrait) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        KycProfile profile = kycService.uploadDocument(username, front, back, portrait);

        return ResponseEntity.ok(ApiResponse.<KycProfile>builder()
                .success(true)
                .message("eKYC documents uploaded successfully. Status: PENDING")
                .data(profile)
                .build());
    }

    @PutMapping("/approve/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<KycProfile>> approveKyc(
            @PathVariable Long id,
            @Valid @RequestBody KycApproveRequest request) {

        KycProfile profile = kycService.approveKyc(id, request);

        return ResponseEntity.ok(ApiResponse.<KycProfile>builder()
                .success(true)
                .message("eKYC processed successfully")
                .data(profile)
                .build());
    }
}