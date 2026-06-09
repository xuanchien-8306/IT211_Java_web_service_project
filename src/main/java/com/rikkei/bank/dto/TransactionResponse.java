package com.rikkei.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private String content;
    private LocalDateTime timestamp;
    private String type; // Tự tính toán (DEBIT - trừ tiền, CREDIT - cộng tiền) ở tầng Service
}