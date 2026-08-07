package com.ktb.lukas.controller;

import jakarta.validation.Valid;
import com.ktb.lukas.dto.PostRequestDto;
import com.ktb.lukas.dto.PostResponseDto;
import com.ktb.lukas.api.ApiResponse;
import com.ktb.lukas.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PostResponseDto> createPost(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody PostRequestDto request
    ) {

        PostResponseDto result = postService.createPost(userId, request);
        return ApiResponse.success(
                "게시글 등록 성공",
                result
        );
    }

    // 전체 게시글 조회
    @GetMapping
    public ApiResponse<List<PostResponseDto>> getPosts(
            @RequestParam(value = "page", defaultValue = "0") int page
    ) {

        // 서비스에서 모든 게시글 리스트를 가져옵니다.
        List<PostResponseDto> result = postService.getPosts(page);

        return ApiResponse.success(
                "모든 게시글 가져오기 성공",
                result
        );
    }


    // 특정 게시글 조회
    @GetMapping("/{postId}")
    public ApiResponse<PostResponseDto> getPost(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId
    ) {

        PostResponseDto result = postService.getPost(userId, postId);

        return ApiResponse.success(
                "게시글 가져오기 성공",
                result
        );
    }
    // 게시글 수정
    @PatchMapping("/{postId}")
    public ApiResponse<PostResponseDto> updatePost(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId,
            @Valid @RequestBody PostRequestDto request
    ) {
        PostResponseDto result = postService.updatePost(userId, postId, request);
        return ApiResponse.success(
                "게시글 업데이트 성공",
                result
        );
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId
    ) {
        postService.deletePost(userId ,postId);
        return ApiResponse.success(
                "게시글 삭제 성공",
                null
        );
    }
}