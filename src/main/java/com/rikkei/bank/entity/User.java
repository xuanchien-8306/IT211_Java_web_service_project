package com.rikkei.bank.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    // Quan hệ N-1: Nhiều User thuộc 1 Role
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    // Quan hệ 1-1: Mỗi User có 1 hồ sơ eKYC
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private KycProfile kycProfile;

    // Quan hệ 1-N: 1 User có thể mở nhiều Account
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Account> accounts;

    @Column(name = "is_kyc", nullable = false, columnDefinition = "boolean default false")
    private boolean isKyc;}