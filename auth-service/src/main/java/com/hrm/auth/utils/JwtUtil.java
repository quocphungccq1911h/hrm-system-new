package com.hrm.auth.utils;

import com.hrm.auth.security.AuthUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration; // Thời gian sống của Access Token (ms)

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(AuthUserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // Thêm các thông tin cần thiết vào Claims (Payload của JWT)
        claims.put("user_id", userDetails.getUserId());
        claims.put("roles", userDetails.getAuthorities());    // Sẽ thêm Roles sau
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject) // tên đăng nhập
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Lấy tên đăng nhập (Subject) từ Token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Lấy một Claim cụ thể từ Token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Lấy toàn bộ Claims (Payload) từ Token
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getPayload();
    }

    // Kiểm tra Token còn hạn không
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    // Hàm Validation cuối cùng
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // Kiểm tra username có khớp và Token còn hạn không
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // Phương thức XÁC THỰC TOKEN (phân tích cú pháp và kiểm tra hết hạn)
    public void validateToken(final String token) {
        Jwts.parser().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
    }

    // Getter cho thời gian hết hạn (sẽ dùng trong AuthResponse)
    public long getExpirationTime() {
        return expiration;
    }

}


