package com.ktb.lukas.controller;

import com.ktb.lukas.api.ApiResponse;
import com.ktb.lukas.dto.PostResponseDto;
import com.ktb.lukas.entity.PostDocument;
import com.ktb.lukas.service.PostSearchService;
import com.ktb.lukas.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts/search")
public class PostSearchController {

    private final PostSearchService postSearchService;

    @GetMapping
    public ApiResponse<List<PostResponseDto>> getPostByKeyword(
            @RequestParam String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page
    ) {
        List<PostResponseDto> responseDto = postSearchService.searchPostsByKeyword(keyword, page);
        return ApiResponse.success("게시글 검색 성공", responseDto);
    }
}
