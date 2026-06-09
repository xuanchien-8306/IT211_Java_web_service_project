// File: src/main/java/com/rikkei/bank/entity/User.java
package com.rikkei.bank.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users") // Lưu ý: Trong Database Schema gốc tên bảng là USER, nếu dùng "users" hãy đảm bảo đồng bộ
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

    private String email;

    private String phoneNumber;

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private KycProfile kycProfile;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Account> accounts;

    @Column(name = "is_kyc", nullable = false, columnDefinition = "boolean default false")
    private Boolean isKyc;
}