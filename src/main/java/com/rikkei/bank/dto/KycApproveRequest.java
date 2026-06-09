package com.rikkei.bank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class KycApproveRequest {
    @NotBlank(message = "Trạng thái không được để trống")
    @Pattern(regexp = "^(CONFIRM|REJECT)$", message = "Trạng thái chỉ được là CONFIRM hoặc REJECT")
    private String status;
}