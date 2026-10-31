package com.ktb.lukas.controller;

import com.ktb.lukas.api.ApiResponse;
import com.ktb.lukas.dto.CommentRequest;
import com.ktb.lukas.dto.CommentResponse;
import com.ktb.lukas.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class CommentController {
    private final CommentService commentservice;

    @PostMapping("/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CommentResponse> createComment(
            @PathVariable Long postId,
            @AuthenticationPrincipal Long userId,   // 로그인 사용자 정보
            @Valid @RequestBody CommentRequest request
    ) {

        CommentResponse result = commentservice.createComment(userId, postId, request);
        return ApiResponse.success(
                "댓글 등록 성공",
                result
        );
    }

    @GetMapping("/{postId}/comments")
    public ApiResponse<List<CommentResponse>> getComments(@PathVariable Long postId) {

        List<CommentResponse> result = commentservice.getComments(postId);

        return ApiResponse.success(
                "댓글 가져오기 성공",
                result
        );
    }
    @PatchMapping("/{postId}/comments/{commentId}")
    public  ApiResponse<CommentResponse> updateComment(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest request
    ) {

        CommentResponse result = commentservice.updateComment(userId, postId, commentId, request);
        return ApiResponse.success(
                "댓글 업데이트 성공",
                result
        );
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ApiResponse<Void> deleteComment(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        commentservice.deleteComment(userId, postId, commentId);
        return ApiResponse.success(
                "댓글 삭제 성공",
                null
        );
    }
}
