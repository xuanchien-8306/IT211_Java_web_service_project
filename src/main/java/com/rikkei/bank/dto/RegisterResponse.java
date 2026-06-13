package com.rikkei.bank.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponse {
    private Long id;
    private String username;
    private String fullName;
    private String idNumber;
    private String role;
    private Boolean isKyc;
}