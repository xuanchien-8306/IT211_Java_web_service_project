package com.rikkei.bank.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String email;
    private String phoneNumber;
    private Boolean isActive;
    private Long roleId;
}
