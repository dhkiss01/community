package com.ktb.lukas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UserRequestDto {

    @Email                              // 이메일 형식 검증 어노테이션
    @NotBlank                           // 공백 여부 어노테이션
    @Size(max = 40)
    private String email;

    @NotBlank
    @Size(min = 8)                      // 최소 8자리는 있어야 한다는 조건 어노테이션 (max도 가능)
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).{8,20}$",
            message = "비밀번호는 8자 이상, 20자 이하이며, 대문자, 소문자, 숫자, 특수문자를 각각 최소 1개 포함해야 합니다."
    )
    private String password;

    @NotBlank                           // 공백 여부 어노테이션
    @Size(max = 10, message = "닉네임은 최대 10자 까지 작성 가능합니다.")
    @Pattern(regexp = "^\\S+$", message = "띄어쓰기를 없애주세요.")
    private String nickname;

    private String image;
}
