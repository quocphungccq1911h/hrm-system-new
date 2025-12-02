package com.hrm.auth.controller;

import com.hrm.auth.dto.AuthResponse;
import com.hrm.auth.dto.LoginRequest;
import com.hrm.auth.dto.RefreshTokenRequest;
import com.hrm.auth.model.User;
import com.hrm.auth.service.AuthService;
import com.hrm.auth.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@Valid @RequestBody User user) {
        // Lưu ý: Trong thực tế, nên dùng RegisterRequest DTO để ẩn các trường không cần thiết
        User createdUser = authService.registerNewUser(user);

        // Loại bỏ mật khẩu trước khi trả về
        createdUser.setPasswordHash(null);

        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        // Gọi dịch vụ để xác thực refresh token và trả về cặp token mới
        AuthResponse response = refreshTokenService.refreshAccessToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    // Endpoint 3: Xác thực Token (Dành cho API Gateway gọi nội bộ)
    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestParam("token") String token) {
        try {
            authService.validateToken(token); // Nếu lỗi sẽ ném Exception
            return ResponseEntity.ok(true); // Token hợp lệ
        } catch (Exception e) {
            return ResponseEntity.status(401).body(false); // Token không hợp lệ
        }
    }
}
