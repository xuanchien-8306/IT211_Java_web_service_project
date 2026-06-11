package com.rikkei.bank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChangePinRequest {
    @NotBlank(message = "Mã PIN cũ không được để trống")
    private String oldPin;

    @NotBlank(message = "Mã PIN mới không được để trống")
    @Pattern(regexp = "^\\d{6}$", message = "Mã PIN phải bao gồm đúng 6 chữ số")
    private String newPin;

    @NotBlank(message = "Xác nhận mã PIN không được để trống")
    private String confirmPin;
}