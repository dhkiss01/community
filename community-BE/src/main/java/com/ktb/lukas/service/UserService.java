package com.ktb.lukas.service;

import com.ktb.lukas.dto.*;
import com.ktb.lukas.entity.User;
import com.ktb.lukas.exception.CustomException;
import com.ktb.lukas.exception.ErrorCode;
import com.ktb.lukas.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원 가입
    @Transactional
    public UserResponseDto createUser(UserRequestDto request) {
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
        if(userRepository.existsByNickname(request.getNickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getNickname(),
                request.getImage()
        );
        User savedUser = userRepository.save(user);
        return new UserResponseDto(savedUser);
    }
    // 회원 정보 조회
    @Transactional(readOnly = true)
    public UserResponseDto getUser(Long userId) {
        User user = findId(userId);
        return new UserResponseDto(user);
    }

    // 회원 정보 수정
    // 닉네임 또는 프로필 이미지를 수정
    @Transactional
    public UserResponseDto updateSet(Long userId, ProfileUpdateRequest request) {
        User user = findId(userId);

        // 닉네임이랑 이미지 둘다 안보내면 예외발생
        if (request.getNickname() == null && request.getImage() == null) {
            throw new CustomException(ErrorCode.MISSING_PROFILE_UPDATE_FIELD);
        }
        // 닉네임 받아와서 현재 닉네임이랑 다를때만 실행해라
        String nickname = request.getNickname();
        if (nickname != null && !nickname.equals(user.getNickname())) {
            // 닉네임 비어있으면 에러
            if (nickname.isBlank()) {
                throw new CustomException(ErrorCode.EMPTY_NICKNAME);
            }
            // 닉네임 중복 검사
            if (userRepository.existsByNickname(nickname)) {
                throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
            }
            user.changeNickname(nickname);
        }
        // 이미지가 들어왔을때만 변경하기 위함
        if (request.getImage() != null) {
            user.changeImage(request.getImage());
        }
        return new UserResponseDto(user);
    }

    // 회원 삭제
    @Transactional
    public void deleteUser(Long userId) {
        User user = findId(userId);
        userRepository.delete(user);
    }

    // 이메일 중복체크
    @Transactional(readOnly = true)
    public void checkEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new CustomException(ErrorCode.EMPTY_EMAIL);
        }
        if (userRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
    }

    // 닉네임 중복체크
    @Transactional(readOnly = true)
    public void checkNickname(String nickname) {

        if (nickname == null || nickname.isBlank()) {
            throw new CustomException(ErrorCode.EMPTY_NICKNAME);
        }

        if (userRepository.existsByNickname(nickname)) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }
    }
    private User findId(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException( ErrorCode.USER_NOT_FOUND));
    }
    // 유저아이디

}