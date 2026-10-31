package com.ktb.lukas.controller;

import com.ktb.lukas.api.ApiResponse;
import com.ktb.lukas.auth.JwtProperties;
import com.ktb.lukas.dto.*;
import com.ktb.lukas.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtProperties jwtProperties;
    @Value("${app.auth.secure-cookie:true}")
    private boolean secureCookie;

    @PostMapping("/auth")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        LoginResult result = authService.login(request);
        setRefreshCookie(response, result.getRefreshToken(), jwtProperties.getRefreshTokenExpSeconds());
        return ApiResponse.success("로그인 성공", result.getResponse());
    }

    @GetMapping("/auth")
    public ApiResponse<Long> check(@AuthenticationPrincipal Long userId) {
        return ApiResponse.success("인증 성공", userId);
    }

    @PostMapping("/users/token/refresh")
    public ApiResponse<TokenInfo> refreshAccess(@CookieValue(name = "refreshToken", required = false) String token,
                                               HttpServletResponse response) {
        TokenResult result = authService.refreshAccessToken(token);
        setRefreshCookie(response, result.getNewRefreshToken(), jwtProperties.getRefreshTokenExpSeconds());
        return ApiResponse.success("토큰 재발급 성공", result.getToken());
    }

    @DeleteMapping("/auth")
    public ApiResponse<Void> logout(@CookieValue(name = "refreshToken", required = false) String token,
                                    HttpServletResponse response) {
        authService.logout(token);
        setRefreshCookie(response, "", 0);
        return ApiResponse.success("로그아웃 성공", null);
    }

    private void setRefreshCookie(HttpServletResponse response, String token, long maxAge) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", token)
                .httpOnly(true).secure(secureCookie).path("/").maxAge(maxAge).sameSite("Lax").build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
