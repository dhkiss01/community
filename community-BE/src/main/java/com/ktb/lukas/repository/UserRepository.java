package com.ktb.lukas.repository;
import com.ktb.lukas.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email); // 이메일 중복검사 메서드
    boolean existsByNickname(String nickname);
}