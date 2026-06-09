package com.rikkei.bank.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "kyc_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String idNumber;
    private String fullName;
    private LocalDate dob;
    private String sex;
    private String address;
    private String idCardFrontUrl;
    private String idCardBackUrl;
    private String portraitUrl;
    @Enumerated(EnumType.STRING)
    private KycStatus status;
    private LocalDateTime verifiedAt;
    private LocalDateTime createdAt;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(mappedBy = "kycProfile", cascade = CascadeType.ALL)
    private RefreshToken refreshToken;
}