package com.ktb.lukas.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentRequest {

    @NotBlank
    @jakarta.validation.constraints.Size(max = 1000)
    private String comment;


}
