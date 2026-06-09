package com.rikkei.bank.repository;

import com.rikkei.bank.entity.KycProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KycProfileRepository extends JpaRepository<KycProfile, Long> {
    // Kiểm tra xem số CMND/CCCD đã tồn tại trong hệ thống chưa
    boolean existsByIdNumber(String idNumber);}