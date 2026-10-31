package com.ktb.lukas.service;

import com.ktb.lukas.dto.PostResponseDto;
import com.ktb.lukas.exception.CustomException;
import com.ktb.lukas.exception.ErrorCode;
import com.ktb.lukas.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostReadService {
    private final PostRepository postRepository;

    public PostResponseDto getPost(Long postId) {
        return new PostResponseDto(postRepository.findWithAuthorById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND)));
    }

    public List<PostResponseDto> searchDatabase(String keyword, Pageable pageable) {
        return postRepository.findByKeyword(keyword, pageable).stream().map(PostResponseDto::new).toList();
    }

    public List<PostResponseDto> getSearchResults(List<Long> ids) {
        Map<Long, PostResponseDto> posts = postRepository.findAllWithAuthorByIdIn(ids).stream()
                .map(PostResponseDto::new)
                .collect(Collectors.toMap(PostResponseDto::getId, Function.identity()));
        return ids.stream().filter(posts::containsKey).map(posts::get).toList();
    }
}
