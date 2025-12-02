package com.hrm.auth.service;

import com.hrm.auth.dto.AuthResponse;
import com.hrm.auth.model.RefreshToken;
import com.hrm.auth.repository.RefreshTokenRepository;
import com.hrm.auth.security.AuthUserDetails;
import com.hrm.auth.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    /**
     * 1. Tạo Refresh Token mới và lưu vào Redis.
     * 2. Xóa token cũ của người dùng nếu tồn tại (đảm bảo chỉ có 1 refresh token hoạt động/user).
     *
     * @param userId ID của người dùng.
     * @return Chuỗi Refresh Token mới.
     */
    @Transactional
    public String createAndSaveRefreshToken(UUID userId) {
        // 💡 Xóa token cũ của người dùng (nếu có, ví dụ khi Login lại)
        refreshTokenRepository.findByUserId(userId)
                .ifPresent(refreshTokenRepository::delete);

        RefreshToken refreshToken = new RefreshToken();
        // Tạo UUID ngẫu nhiên làm token string
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUserId(userId);

        // Lưu vào Redis (TTL Time-To-Live sẽ được Redis quản lý, thường là 7 ngày)
        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }

    /**
     * 2. Xác thực Refresh Token cũ và tạo cặp Access/Refresh Token mới (Token Rotation).
     * @param oldRefreshToken Refresh Token cũ được gửi từ client.
     * @return Cặp AuthResponse chứa Access Token và Refresh Token mới.
     */
    public AuthResponse refreshAccessToken(String oldRefreshToken) {
        // 💡 Bước 1: Tìm Refresh Token trong Redis
        RefreshToken token = refreshTokenRepository.findById(oldRefreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid or expired refresh token. Please log in again."));

        // 💡 Bước 2: Load User (AuthUserDetails) bằng ID đã lưu trong Redis
        // Load user để có thông tin hiện tại và Authorities
        AuthUserDetails userDetails;
        try {
            userDetails = (AuthUserDetails) userDetailsService.loadUserByUsername(
                    token.getUserId().toString() // LoadUserByUsername thường nhận String (UUID.toString())
            );
        } catch (Exception e) {
            // Xóa token nếu user không còn tồn tại
            refreshTokenRepository.delete(token);
            throw new RuntimeException("User associated with refresh token not found.");
        }
        // 💡 Bước 3: Tạo Access Token mới
        String newAccessToken = jwtUtil.generateToken(userDetails);

        // 💡 Bước 4: Xóa token CŨ và tạo token MỚI (Rotation)
        // Token Rotation: Xóa token cũ ngay lập tức (giúp ngăn chặn tấn công Replay Attack)
        refreshTokenRepository.delete(token);
        String newRefreshToken = createAndSaveRefreshToken(token.getUserId());

        // 💡 Bước 5: Trả về Response
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                // Giả định JwtUtil có getter cho thời gian hết hạn (theo giây)
                .expiresIn(jwtUtil.getExpirationTime())
                .build();
    }

}
