package com.rikkei.bank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAccountRequest {
    @NotBlank(message = "Mã PIN không được để trống")
    @Size(min = 6, max = 6, message = "Mã PIN phải bao gồm đúng 6 chữ số")
    private String pin;
}