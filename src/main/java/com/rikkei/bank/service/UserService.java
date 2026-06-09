package com.rikkei.bank.service;

import com.rikkei.bank.dto.UserCreateRequest;
import com.rikkei.bank.dto.UserUpdateRequest;
import com.rikkei.bank.dto.UserResponseDto;
import org.springframework.data.domain.Page;

public interface UserService {
    Page<UserResponseDto> getAllUsers(int page, int size);
    UserResponseDto getUserById(Long id);
    UserResponseDto createUser(UserCreateRequest request);
    UserResponseDto updateUser(Long id, UserUpdateRequest request);
    void deleteUser(Long id);
}
