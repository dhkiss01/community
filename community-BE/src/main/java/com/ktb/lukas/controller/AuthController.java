package com.ktb.lukas.controller;

import com.ktb.lukas.api.ApiResponse;
import com.ktb.lukas.dto.*;
import com.ktb.lukas.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    @org.springframework.beans.factory.annotation.Value("${app.auth.secure-cookie:true}")
    private boolean secureCookie;

    // 로그인
    @PostMapping("/auth")
    public ApiResponse<LoginResponse> login(

            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse httpResponse
    ) {
        LoginResult result = authService.login(loginRequest);

        ResponseCookie refreshCookie = ResponseCookie
                .from("refreshToken", result.getRefreshToken())
                .httpOnly(true)
                .secure(secureCookie)
                .path("/")
                .maxAge(14 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();

        httpResponse.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ApiResponse.success(
                "로그인 성공",
                result.getResponse()
        );
    }

    @GetMapping("/auth")
    public ApiResponse<Long> check(@AuthenticationPrincipal Long userId) {


        return ApiResponse.success(
                "인증 성공",
                userId
        );
    }

    @PostMapping("/users/token/refresh")
    public ApiResponse<TokenInfo> refreshAccess(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse httpResponse
    ) {
        TokenResult result = authService.refreshAccessToken(refreshToken);

        // Refresh Token 회전 시 새 쿠키 세팅
        if (result.getNewRefreshToken() != null) {
            ResponseCookie cookie = ResponseCookie.from("refreshToken", result.getNewRefreshToken())
                    .httpOnly(true)
                    .secure(secureCookie)
                    .path("/")
                    .maxAge(14 * 24 * 60 * 60)
                    .sameSite("Lax")
                    .build();
            httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }

        return ApiResponse.success(
                "토큰 재발급 성공",
                result.getToken()
        );
    }
}