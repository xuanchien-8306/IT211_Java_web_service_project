package com.rikkei.bank.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "kyc_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    private String idCardNumber;

    @Column(nullable = false)
    private String status;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @OneToOne(mappedBy = "kycProfile", cascade = CascadeType.ALL)
    private RefreshToken refreshToken;
}