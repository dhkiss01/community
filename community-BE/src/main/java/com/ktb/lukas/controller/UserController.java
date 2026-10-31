package com.ktb.lukas.controller;

import com.ktb.lukas.exception.CustomException;
import com.ktb.lukas.exception.ErrorCode;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.ktb.lukas.dto.*;
import com.ktb.lukas.api.ApiResponse;
import com.ktb.lukas.service.UserService;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto request) {
        UserResponseDto response = userService.createUser(request);
        return new ApiResponse<>(
                "SUCCESS",
                "회원가입 성공",
                response
        );
    }

    @GetMapping("/{userId}")
    public UserResponseDto getUser(@AuthenticationPrincipal Long actorId, @PathVariable Long userId) {
        requireOwner(actorId, userId);
        return userService.getUser(userId);
    }

    @PatchMapping("/{userId}")
    public UserResponseDto updateSet(
            @AuthenticationPrincipal Long actorId,
            @PathVariable Long userId,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        requireOwner(actorId, userId);
        return userService.updateSet(userId, request);
    }

    @DeleteMapping("/{userId}")
    public ApiResponse<Void> deleteUser(@AuthenticationPrincipal Long actorId, @PathVariable Long userId) {
        requireOwner(actorId, userId);
        userService.deleteUser(userId);
        return new ApiResponse(
                "SUCCESS",
                "회원 삭제 완료",
                null
        );
    }


    private void requireOwner(Long actorId, Long userId) {
        if (!userId.equals(actorId)) throw new CustomException(ErrorCode.FORBIDDEN);
    }

    @GetMapping
    public ApiResponse<String> checkDuplicate(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String nickname
    ) {

        if (email != null) {
            userService.checkEmail(email);
            return new ApiResponse<>(
                    "SUCCESS",
                    "사용 가능한 이메일입니다.",
                    "email_available"
            );
        }

        if (nickname != null) {
            userService.checkNickname(nickname);
            return new ApiResponse<>(
                    "SUCCESS",
                    "사용 가능한 닉네임입니다.",
                    "nickname_available"
            );
        }
        throw new CustomException(ErrorCode.EMPTY_EMAIL);
    }
}