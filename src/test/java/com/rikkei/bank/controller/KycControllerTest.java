package com.rikkei.bank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rikkei.bank.dto.KycApproveRequest;
import com.rikkei.bank.entity.KycProfile;
import com.rikkei.bank.service.KycService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(KycController.class)
@AutoConfigureMockMvc(addFilters = false) // Tắt Spring Security để test độc lập logic Controller
class KycControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KycService kycService;

    @Test
    void approveKyc_ValidConfirmRequest_Returns200() throws Exception {
        KycApproveRequest request = new KycApproveRequest();
        request.setStatus("CONFIRM");

        when(kycService.approveKyc(eq(1L), any(KycApproveRequest.class))).thenReturn(new KycProfile());

        mockMvc.perform(put("/api/v1/kyc/approve/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("eKYC processed successfully"));
    }

    // Test 7: Controller nhận body "REJECT" hợp lệ -> Trả về 200 OK
    @Test
    void approveKyc_ValidRejectRequest_Returns200() throws Exception {
        KycApproveRequest request = new KycApproveRequest();
        request.setStatus("REJECT");

        when(kycService.approveKyc(eq(1L), any(KycApproveRequest.class))).thenReturn(new KycProfile());

        mockMvc.perform(put("/api/v1/kyc/approve/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // Test 8: Controller chặn khi thiếu thuộc tính status (Validate @NotBlank) -> Trả về 400
    @Test
    void approveKyc_MissingStatusField_Returns400() throws Exception {
        KycApproveRequest request = new KycApproveRequest();
        request.setStatus(""); // Cố tình để rỗng

        mockMvc.perform(put("/api/v1/kyc/approve/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // Do bật @Valid ở Controller
    }

    // Test 9: Controller chặn khi gõ sai chữ CONFIRM/REJECT (Validate @Pattern) -> Trả về 400
    @Test
    void approveKyc_InvalidStatusString_Returns400() throws Exception {
        KycApproveRequest request = new KycApproveRequest();
        request.setStatus("DUYET"); // Chữ không nằm trong Regex

        mockMvc.perform(put("/api/v1/kyc/approve/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // Test 10: Controller chặn khi không gửi Body -> Trả về 400 Bad Request
    @Test
    void approveKyc_NoBody_Returns400() throws Exception {
        mockMvc.perform(put("/api/v1/kyc/approve/1")
                        .contentType(MediaType.APPLICATION_JSON))
                // Bỏ trống hoàn toàn .content()
                .andExpect(status().isBadRequest());
    }
}