package com.rikkei.bank.config;

import com.rikkei.bank.entity.*;
import com.rikkei.bank.repository.AccountRepository;
import com.rikkei.bank.repository.RoleRepository;
import com.rikkei.bank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. Tạo Roles
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role(null, "ROLE_ADMIN", null));
            roleRepository.save(new Role(null, "ROLE_CUSTOMER", null));
            roleRepository.save(new Role(null, "ROLE_STAFF", null));
            log.info("=> Seeded Roles.");
        }

        // 2. Tạo Admin
        if (!userRepository.existsByUsername("admin")) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").get();
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("123456"))
                    .role(adminRole)
                    .isKyc(true)
                    .build();
            userRepository.save(admin);
            log.info("=> Seeded Admin User.");
        }

        // 3. Tạo Customer 1 (Kèm Account có sẵn 5 triệu để test chuyển khoản)
        if (!userRepository.existsByUsername("customer1")) {
            Role customerRole = roleRepository.findByName("ROLE_CUSTOMER").get();

            User customer1 = User.builder()
                    .username("customer1")
                    .password(passwordEncoder.encode("123456"))
                    .role(customerRole)
                    .isKyc(true)
                    .build();

            KycProfile kyc1 = KycProfile.builder()
                    .fullName("Nguyễn Văn A")
                    .idNumber("001200300400")
                    .status(KycStatus.CONFIRM)
                    .user(customer1)
                    .build();
            customer1.setKycProfile(kyc1);

            Account acc1 = Account.builder()
                    .accountNumber("100000001")
                    .balance(new BigDecimal("5000000")) // Có sẵn 5 triệu
                    .pin(passwordEncoder.encode("123456"))
                    .status("ACTIVE")
                    .user(customer1)
                    .build();

            userRepository.save(customer1);
            accountRepository.save(acc1);
            log.info("=> Seeded Customer 1 (Acc: 100000001 - Balance: 5M).");
        }

        // 4. Tạo Customer 2 (Kèm Account 0 đồng để nhận tiền)
        if (!userRepository.existsByUsername("customer2")) {
            Role customerRole = roleRepository.findByName("ROLE_CUSTOMER").get();

            User customer2 = User.builder()
                    .username("customer2")
                    .password(passwordEncoder.encode("123456"))
                    .role(customerRole)
                    .isKyc(true)
                    .build();

            KycProfile kyc2 = KycProfile.builder()
                    .fullName("Trần Thị B")
                    .idNumber("002200300500")
                    .status(KycStatus.CONFIRM)
                    .user(customer2)
                    .build();
            customer2.setKycProfile(kyc2);

            Account acc2 = Account.builder()
                    .accountNumber("100000002")
                    .balance(BigDecimal.ZERO)
                    .pin(passwordEncoder.encode("123456"))
                    .status("ACTIVE")
                    .user(customer2)
                    .build();

            userRepository.save(customer2);
            accountRepository.save(acc2);
            log.info("=> Seeded Customer 2 (Acc: 100000002 - Balance: 0).");
        }
    }
}