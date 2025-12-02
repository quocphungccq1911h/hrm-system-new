package com.hrm.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 🔒 Tắt CSRF (Thường dùng cho Web, không cần thiết cho Mobile App/REST API)
                .csrf(AbstractHttpConfigurer::disable)
                // 🚪 Cho phép truy cập công khai (permitAll) các đường dẫn của Authen
                .authorizeHttpRequests(authorize -> authorize
                        // Đường dẫn /api/v1/auth/login và /api/v1/auth/register được phép truy cập mà không cần Token
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // Tất cả các request khác (đường dẫn khác) phải được xác thực
                        .anyRequest().authenticated()
                )
                // 🚫 Tắt form login mặc định của Spring
                .httpBasic(Customizer.withDefaults());
        // (Sẽ cấu hình SessionManagement và Filter JWT ở bước sau)

        return http.build();
    }

    // 💡 Bean để mã hóa mật khẩu
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }
}
