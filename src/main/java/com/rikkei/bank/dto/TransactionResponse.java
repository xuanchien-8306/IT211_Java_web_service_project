package com.rikkei.bank.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonIgnore
    private BigDecimal amount;

    @JsonProperty("amount")
    private String formattedAmount;

    private String content;
    private LocalDateTime timestamp;
    private String type;
}