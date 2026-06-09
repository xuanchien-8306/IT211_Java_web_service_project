package com.rikkei.bank.repository;

import com.rikkei.bank.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {
    // Kiểm tra xem token này đã bị thu hồi (nằm trong blacklist) hay chưa
    boolean existsByToken(String token);
}