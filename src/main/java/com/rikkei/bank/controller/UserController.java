package com.rikkei.bank.controller;

import com.rikkei.bank.dto.UserCreateRequest;
import com.rikkei.bank.dto.UserUpdateRequest;
import com.rikkei.bank.dto.ApiResponse;
import com.rikkei.bank.dto.UserResponseDto;
import com.rikkei.bank.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponseDto>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.<Page<UserResponseDto>>builder()
                .success(true)
                .message("Fetched users successfully")
                .data(userService.getAllUsers(page, size))
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<UserResponseDto>builder()
                .success(true)
                .message("Fetched user successfully")
                .data(userService.getUserById(id))
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(@RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.<UserResponseDto>builder()
                .success(true)
                .message("User created successfully")
                .data(userService.createUser(request))
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(
            @PathVariable Long id,
            @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.<UserResponseDto>builder()
                .success(true)
                .message("User updated successfully")
                .data(userService.updateUser(id, request))
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("User deactivated successfully")
                .build());
    }
}