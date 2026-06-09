package com.rikkei.bank.dto;

import lombok.Data;

@Data
public class UserCreateRequest {
    private String username;
    private String password;
    private String email;
    private String phoneNumber;
    private Long roleId;
}
