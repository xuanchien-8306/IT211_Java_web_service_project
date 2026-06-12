package com.rikkei.bank.service.impl;

import com.rikkei.bank.dto.KycApproveRequest;
import com.rikkei.bank.dto.ApiResponse;
import com.rikkei.bank.entity.KycProfile;
import com.rikkei.bank.entity.KycStatus;
import com.rikkei.bank.entity.User;
import com.rikkei.bank.exception.UserNotFoundException;
import com.rikkei.bank.repository.UserRepository;
import com.rikkei.bank.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public ApiResponse<String> approveKyc(Long userId, KycApproveRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        KycProfile kycProfile = user.getKycProfile();

        kycProfile.setStatus(KycStatus.CONFIRM);
        if ("CONFIRM".equals(request.getStatus())) {
            user.setIsKyc(true);
        } else {
            user.setIsKyc(false);
        }

        userRepository.save(user);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Đã cập nhật trạng thái hồ sơ eKYC thành: " + request.getStatus())
                .build();
    }
}