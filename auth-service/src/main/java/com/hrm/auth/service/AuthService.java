package com.hrm.auth.service;

import com.hrm.auth.dto.AuthResponse;
import com.hrm.auth.dto.LoginRequest;
import com.hrm.auth.mapper.UserMapper;
import com.hrm.auth.model.User;
import com.hrm.auth.security.AuthUserDetails;
import com.hrm.auth.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public User registerNewUser(User user) {
        try {
            // 🔒 Mã hóa mật khẩu trước khi lưu vào DB
            String encodedPassword = passwordEncoder.encode(user.getPasswordHash());
            user.setPasswordHash(encodedPassword);
            user.setCreatedAt(new Date());

            userMapper.insert(user);
            return user;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public AuthResponse authenticate(LoginRequest request) {
        // 🔑 Bước 1: Xác thực bằng Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        // 🔑 Bước 2: Lấy thông tin User đã xác thực
        AuthUserDetails userDetails = (AuthUserDetails) authentication.getPrincipal();

        // 🔑 Bước 3: Tạo JWT Access Token
        String accessToken = jwtUtil.generateToken(userDetails);

        // 🔑 Bước 4: Tạo Refresh Token (Tạm thời chỉ trả về rỗng, logic lưu Redis sẽ làm sau)
        String refreshToken = "REFRESH_" + userDetails.getUserId();

        // 🔑 Bước 5: Trả về DTO Response
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(3600L)
                .build();
    }

    public Boolean validateToken(String token) {
        try {
            jwtUtil.validateToken(token); // Nếu lỗi sẽ ném Exception
            return true; // Token hợp lệ
        } catch (Exception e) {
            return false;// Token không hợp lệ
        }
    }
}
