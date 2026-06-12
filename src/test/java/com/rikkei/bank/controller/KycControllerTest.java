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

import com.rikkei.bank.security.JwtFilter;
import com.rikkei.bank.security.JwtProvider;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(KycController.class)
@AutoConfigureMockMvc(addFilters = false)
class KycControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KycService kycService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private JwtFilter jwtFilter;

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
                .andExpect(jsonPath("$.message").value("Xử lý hồ sơ eKYC thành công"));
    }

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

    @Test
    void approveKyc_MissingStatusField_Returns400() throws Exception {
        KycApproveRequest request = new KycApproveRequest();
        request.setStatus("");

        mockMvc.perform(put("/api/v1/kyc/approve/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approveKyc_InvalidStatusString_Returns400() throws Exception {
        KycApproveRequest request = new KycApproveRequest();
        request.setStatus("DUYET");

        mockMvc.perform(put("/api/v1/kyc/approve/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approveKyc_NoBody_Returns400() throws Exception {
        mockMvc.perform(put("/api/v1/kyc/approve/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}