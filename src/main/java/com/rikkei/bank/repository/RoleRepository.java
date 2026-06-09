package com.rikkei.bank.repository;

import com.rikkei.bank.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    // Tìm kiếm Role theo tên (VD: ROLE_USER, ROLE_ADMIN) để gán quyền khi đăng ký
    Optional<Role> findByName(String name);
}