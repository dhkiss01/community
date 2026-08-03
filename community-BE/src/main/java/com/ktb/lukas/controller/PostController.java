package com.ktb.lukas.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ktb.lukas.api.ApiResponse;
import com.ktb.lukas.dto.PostRequestDto;
import com.ktb.lukas.dto.PostResponseDto;
import com.ktb.lukas.service.PostService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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

    // 검색 기능
    @GetMapping("/search")
    public ApiResponse<List<PostResponseDto>> getPostByKeyword(@RequestParam String keyword,
                                                         @RequestParam(value = "page", defaultValue = "0") int page,
                                                         @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        List<PostResponseDto> result = postService.getPostsBySearch(keyword, page);

        return ApiResponse.success(
            "게시글 검색 성공",
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