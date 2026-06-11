package com.rikkei.bank.service.impl;

import com.rikkei.bank.dto.KycApproveRequest;
import com.rikkei.bank.entity.KycProfile;
import com.rikkei.bank.entity.KycStatus;
import com.rikkei.bank.entity.User;
import com.rikkei.bank.exception.BusinessException;
import com.rikkei.bank.repository.KycProfileRepository;
import com.rikkei.bank.repository.UserRepository;
import com.rikkei.bank.service.KycService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class KycServiceImpl implements KycService {

    private final KycProfileRepository kycProfileRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public KycProfile uploadDocument(String username, MultipartFile front, MultipartFile back, MultipartFile portrait) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("User not found"));

        validateFile(front);
        validateFile(back);
        validateFile(portrait);

        long timestamp = System.currentTimeMillis();
        String frontUrl = "https://cloudinary.com/rikkei/front_" + timestamp + "_" + front.getOriginalFilename();
        String backUrl = "https://cloudinary.com/rikkei/back_" + timestamp + "_" + back.getOriginalFilename();
        String portraitUrl = "https://cloudinary.com/rikkei/portrait_" + timestamp + "_" + portrait.getOriginalFilename();

        KycProfile profile = user.getKycProfile();
        if (profile == null) {
            profile = new KycProfile();
            profile.setUser(user);
            profile.setCreatedAt(LocalDateTime.now());
        }

        profile.setIdCardFrontUrl(frontUrl);
        profile.setIdCardBackUrl(backUrl);
        profile.setPortraitUrl(portraitUrl);
        profile.setStatus(KycStatus.PENDING);

        return kycProfileRepository.save(profile);
    }

    @Override
    @Transactional
    public KycProfile approveKyc(Long kycId, KycApproveRequest request) {
        KycProfile profile = kycProfileRepository.findById(kycId)
                .orElseThrow(() -> new BusinessException("KYC Profile not found"));

        // Chốt chặn 1: Chỉ xử lý hồ sơ đang chờ duyệt
        if (profile.getStatus() != KycStatus.PENDING) {
            throw new BusinessException("Hồ sơ này đã được xử lý (Duyệt hoặc Từ chối) trước đó");
        }

        // Chốt chặn 2: Đọc trạng thái từ DTO của bạn ("CONFIRM" hoặc "REJECT")
        if ("CONFIRM".equals(request.getStatus())) {

            // Luồng duyệt thành công
            profile.setStatus(KycStatus.CONFIRM);
            profile.setVerifiedAt(LocalDateTime.now());

            User user = profile.getUser();
            user.setIsKyc(true);
            userRepository.save(user);

        } else if ("REJECT".equals(request.getStatus())) {

            // Luồng từ chối
            profile.setStatus(KycStatus.REJECT);

        }

        return kycProfileRepository.save(profile);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException("Invalid file or file size exceeds 5MB");
        }
    }
}
