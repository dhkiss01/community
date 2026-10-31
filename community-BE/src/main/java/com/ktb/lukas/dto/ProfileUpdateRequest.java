package com.ktb.lukas.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ProfileUpdateRequest {
    @Size(min = 1, max = 10)
    @Pattern(regexp = "^\\S+$", message = "닉네임에는 공백을 사용할 수 없습니다.")
    private String nickname;
    @Size(max = 200)
    private String image;
}
