package com.rikkei.bank.repository;

import com.rikkei.bank.dto.UserResponseDto;
import com.rikkei.bank.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    @Query("SELECT new com.rikkei.bank.dto.UserResponseDto(u.id, u.username, r.name, u.isKyc) FROM User u JOIN u.role r")
    Page<UserResponseDto> findAllUsersProjection(Pageable pageable);
}