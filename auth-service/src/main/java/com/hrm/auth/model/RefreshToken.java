package com.hrm.auth.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.util.UUID;

@RedisHash(value = "refresh_token", timeToLive = 604800) // TTL 604800 giây = 7 ngày
@Data
public class RefreshToken {
    @Id // Khóa chính của đối tượng Redis (là chính chuỗi token)
    private String token;

    // Dùng @Indexed để có thể tìm kiếm nhanh theo userId
    @Indexed
    private UUID userId;

    // Các trường khác như issueDate, clientIp có thể được thêm vào đây
}
