package com.rikkei.bank.service;

import com.rikkei.bank.dto.KycApproveRequest;
import com.rikkei.bank.entity.KycProfile;
import org.springframework.web.multipart.MultipartFile;

public interface KycService {
    KycProfile uploadDocument(String username, MultipartFile front, MultipartFile back, MultipartFile portrait);
    KycProfile approveKyc(Long kycId, KycApproveRequest request);
}
