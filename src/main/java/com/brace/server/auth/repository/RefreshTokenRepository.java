package com.brace.server.auth.repository;

import com.brace.server.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    void deleteByUserId(Long userId);

    void deleteByUserIdAndTokenHash(Long userId, String tokenHash);
}
