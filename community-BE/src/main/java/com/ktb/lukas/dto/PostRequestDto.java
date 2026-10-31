package com.ktb.lukas.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class PostRequestDto {

    @NotBlank                               // 공백 여부 어노테이션
    @Size(max = 26)
    private String title;

    private com.ktb.lukas.entity.PostCategory category;

    @NotBlank                               // 공백 여부 어노테이션
    @Size(max = 20000)
    private String content;

    @Size(max = 200)
    private String image;
}
