package com.rikkei.bank.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferRequest {

    @NotBlank(message = "Tài khoản nguồn không được để trống")
    private String fromAccount;

    @NotBlank(message = "Tài khoản đích không được để trống")
    private String toAccount;

    @NotNull(message = "Số tiền không hợp lệ")
    @DecimalMin(value = "1000.0", message = "Số tiền chuyển tối thiểu là 1000 VNĐ")
    private BigDecimal amount;

    @NotBlank(message = "Mã PIN không được để trống")
    private String pin;

    private String description;
}