package com.rikkei.bank.config;

import com.rikkei.bank.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Mã hóa mật khẩu an toàn theo chuẩn SRS
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Tắt CSRF sử dụng cấu hình lambda chuẩn Spring Boot 3.x
                .csrf(csrf -> csrf.disable())
                // Cấu hình Session thành STATELESS
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Cấu hình phân quyền API dựa trên Endpoint thực tế của Controller
                .authorizeHttpRequests(auth -> auth
                        // Nhóm Public API công khai hoàn toàn
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // Nhóm Role ADMIN
                        .requestMatchers("/api/v1/admin/**").hasAuthority("ROLE_ADMIN")
                        // Nhóm Role STAFF
                        .requestMatchers("/api/v1/staff/**").hasAuthority("ROLE_STAFF")
                        // Nhóm Core Banking yêu cầu đúng quyền ROLE_CUSTOMER
                        .requestMatchers("/api/v1/account/**", "/api/v1/transaction/**").hasAuthority("ROLE_CUSTOMER")
                        // Tất cả các request còn lại bắt buộc phải xác thực
                        .anyRequest().authenticated()
                );

        // Thêm JwtFilter chặn trước luồng xác thực
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}