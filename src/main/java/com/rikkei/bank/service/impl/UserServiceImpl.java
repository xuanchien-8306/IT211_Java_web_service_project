package com.rikkei.bank.service.impl;

import com.rikkei.bank.dto.UserCreateRequest;
import com.rikkei.bank.dto.UserUpdateRequest;
import com.rikkei.bank.dto.UserResponseDto;
import com.rikkei.bank.entity.Role;
import com.rikkei.bank.entity.User;
import com.rikkei.bank.exception.BusinessException;
import com.rikkei.bank.repository.RoleRepository;
import com.rikkei.bank.repository.UserRepository;
import com.rikkei.bank.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<UserResponseDto> getAllUsers(int page, int size) {
        return userRepository.findAllUsersProjection(PageRequest.of(page, size));
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found"));
        return new UserResponseDto(user.getId(), user.getUsername(), user.getRole().getName(), user.getIsKyc());    }

    @Override
    @Transactional
    public UserResponseDto createUser(UserCreateRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException("Username already exists");
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new BusinessException("Role not found"));

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .isActive(true)
                .isKyc(false)
                .createdAt(LocalDateTime.now())
                .role(role)
                .build();

        user = userRepository.save(user);
        return new UserResponseDto(user.getId(), user.getUsername(), user.getRole().getName(), user.getIsKyc());    }

    @Override
    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found"));

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }

        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new BusinessException("Role not found"));
            user.setRole(role);
        }

        user = userRepository.save(user);

        return new UserResponseDto(user.getId(), user.getUsername(), user.getRole().getName(), user.getIsKyc());
    }
    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found"));
        user.setIsActive(false);
        userRepository.save(user);
    }
}
