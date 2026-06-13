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
                .message("Lấy danh sách người dùng thành công")
                .data(userService.getAllUsers(page, size))
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<UserResponseDto>builder()
                .success(true)
                .message("Lấy thông tin người dùng thành công")
                .data(userService.getUserById(id))
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(@RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.<UserResponseDto>builder()
                .success(true)
                .message("Tạo người dùng thành công")
                .data(userService.createUser(request))
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(
            @PathVariable Long id,
            @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.<UserResponseDto>builder()
                .success(true)
                .message("Cập nhật người dùng thành công")
                .data(userService.updateUser(id, request))
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> deleteUser(@PathVariable Long id) {

        UserResponseDto user = userService.deleteUser(id);

        return ResponseEntity.ok(ApiResponse.<UserResponseDto>builder()
                .success(true)
                .message("Vô hiệu hóa người dùng thành công")
                .data(user)
                .build());
    }
}