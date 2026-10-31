package com.ktb.lukas.service;

import com.ktb.lukas.auth.JwtProperties;
import com.ktb.lukas.auth.JwtProvider;
import com.ktb.lukas.dto.*;
import com.ktb.lukas.entity.RefreshToken;
import com.ktb.lukas.entity.User;
import com.ktb.lukas.exception.CustomException;
import com.ktb.lukas.exception.ErrorCode;
import com.ktb.lukas.repository.RefreshTokenRepository;
import com.ktb.lukas.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;

    public LoginResult login(LoginRequest request) {
        User user = userRepository.findLockedByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }
        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail(), user.getNickname());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());
        refreshTokenRepository.deleteByUserId(user.getId());
        refreshTokenRepository.flush();
        saveRefreshToken(refreshToken, user.getId());
        return new LoginResult(LoginResponse.of(user, accessToken, jwtProperties.getAccessTokenExpSeconds()), refreshToken);
    }

    public TokenResult refreshAccessToken(String token) {
        if (token == null || token.isBlank()) throw new CustomException(ErrorCode.INVALID_TOKEN);
        RefreshToken saved = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));
        if (saved.isExpired()) throw new CustomException(ErrorCode.INVALID_TOKEN);
        User user = userRepository.findById(saved.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));
        String access = jwtProvider.createAccessToken(user.getId(), user.getEmail(), user.getNickname());
        String refresh = jwtProvider.createRefreshToken(user.getId());
        refreshTokenRepository.delete(saved);
        refreshTokenRepository.flush();
        saveRefreshToken(refresh, user.getId());
        return new TokenResult(new TokenInfo(access, jwtProperties.getAccessTokenExpSeconds()), refresh);
    }

    public void logout(String token) {
        if (token != null && !token.isBlank()) refreshTokenRepository.deleteByToken(token);
    }

    private void saveRefreshToken(String token, Long userId) {
        refreshTokenRepository.save(new RefreshToken(token, userId,
                LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTokenExpSeconds())));
    }
}
