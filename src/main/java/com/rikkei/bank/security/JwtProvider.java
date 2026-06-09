package com.rikkei.bank.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
@Slf4j
public class JwtProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // Tạo mã Secret Key chuẩn HMAC-SHA từ chuỗi cấu hình
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // Tạo Access Token dựa trên thông tin User
    public String generateToken(CustomUserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Trích xuất username từ Token
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) getSigningKey()) // Cú pháp mới của 0.12.x
                .build()
                .parseSignedClaims(token) // Cú pháp mới
                .getPayload()
                .getSubject();
    }

    // Kiểm tra tính hợp lệ của Token
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith((javax.crypto.SecretKey) getSigningKey()) // Cú pháp mới của 0.12.x
                    .build()
                    .parseSignedClaims(token); // Cú pháp mới
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Token không đúng định dạng");
        } catch (ExpiredJwtException ex) {
            log.error("Token đã hết hạn");
        } catch (UnsupportedJwtException ex) {
            log.error("Token không được hỗ trợ");
        } catch (IllegalArgumentException ex) {
            log.error("Chuỗi token trống");
        }
        return false;
    }
}