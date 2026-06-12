package com.rikkei.bank.service.impl;

import com.rikkei.bank.dto.KycApproveRequest;
import com.rikkei.bank.entity.KycProfile;
import com.rikkei.bank.entity.KycStatus;
import com.rikkei.bank.entity.User;
import com.rikkei.bank.exception.BusinessException;
import com.rikkei.bank.repository.KycProfileRepository;
import com.rikkei.bank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KycServiceImplTest {

    @Mock
    private KycProfileRepository kycProfileRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private KycServiceImpl kycService;

    private KycProfile pendingProfile;
    private User testUser;
    private KycApproveRequest confirmRequest;
    private KycApproveRequest rejectRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setIsKyc(false);

        pendingProfile = new KycProfile();
        pendingProfile.setId(100L);
        pendingProfile.setStatus(KycStatus.PENDING);
        pendingProfile.setUser(testUser);

        confirmRequest = new KycApproveRequest();
        confirmRequest.setStatus("CONFIRM");

        rejectRequest = new KycApproveRequest();
        rejectRequest.setStatus("REJECT");
    }

    @Test
    void approveKyc_Confirm_Success() {
        when(kycProfileRepository.findById(100L)).thenReturn(Optional.of(pendingProfile));
        when(kycProfileRepository.save(any(KycProfile.class))).thenReturn(pendingProfile);

        KycProfile result = kycService.approveKyc(100L, confirmRequest);

        assertEquals(KycStatus.CONFIRM, result.getStatus());
        assertTrue(testUser.getIsKyc());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void approveKyc_Reject_Success() {
        when(kycProfileRepository.findById(100L)).thenReturn(Optional.of(pendingProfile));
        when(kycProfileRepository.save(any(KycProfile.class))).thenReturn(pendingProfile);

        KycProfile result = kycService.approveKyc(100L, rejectRequest);

        assertEquals(KycStatus.REJECT, result.getStatus());
        assertFalse(testUser.getIsKyc());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void approveKyc_ProfileNotFound_ThrowsException() {
        when(kycProfileRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            kycService.approveKyc(999L, confirmRequest);
        });

        assertEquals("KYC Profile not found", exception.getMessage());
    }

    @Test
    void approveKyc_NotPending_ThrowsException() {
        pendingProfile.setStatus(KycStatus.CONFIRM);
        when(kycProfileRepository.findById(100L)).thenReturn(Optional.of(pendingProfile));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            kycService.approveKyc(100L, confirmRequest);
        });

        assertEquals("Hồ sơ này đã được xử lý (Duyệt hoặc Từ chối) trước đó", exception.getMessage());
    }

    @Test
    void approveKyc_NullRequest_HandlesSafely() {
        when(kycProfileRepository.findById(100L)).thenReturn(Optional.of(pendingProfile));
        when(kycProfileRepository.save(any(KycProfile.class))).thenReturn(pendingProfile);

        KycApproveRequest emptyRequest = new KycApproveRequest();

        assertDoesNotThrow(() -> {
            kycService.approveKyc(100L, emptyRequest);
        });
    }
}