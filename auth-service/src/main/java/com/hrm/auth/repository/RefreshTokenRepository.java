package com.hrm.auth.repository;

import com.hrm.auth.model.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
    /**
     * Tìm kiếm Refresh Token dựa trên ID của User (Nhờ @Indexed trong Model)
     * @param userId ID của người dùng (UUID)
     * @return Optional<RefreshToken>
     */
    Optional<RefreshToken> findByUserId(UUID userId);
}
