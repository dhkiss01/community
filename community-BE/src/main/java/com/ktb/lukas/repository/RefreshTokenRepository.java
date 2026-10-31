package com.ktb.lukas.repository;

import com.ktb.lukas.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    Optional<RefreshToken> findByToken(String token);
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("delete from RefreshToken r where r.token = :token")
    void deleteByToken(@org.springframework.data.repository.query.Param("token") String token);
    void deleteByUserId(Long userId);
}
