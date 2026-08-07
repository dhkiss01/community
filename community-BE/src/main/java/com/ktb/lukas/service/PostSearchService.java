package com.ktb.lukas.service;

import com.ktb.lukas.dto.PostResponseDto;
import com.ktb.lukas.entity.Post;
import com.ktb.lukas.entity.PostDocument;
import com.ktb.lukas.repository.ElasticPostRepository;
import com.ktb.lukas.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class PostSearchService {
    private final ElasticPostRepository elasticPostRepository;
    private final PostRepository postRepository;


    @Transactional(readOnly = true)
    public List<PostResponseDto> searchPostsByKeyword(String keyword, int page) {
        Pageable pageable = PageRequest.of(page, 10);

        // ES에서 title 기반으로 PostDocument 검색
        List<PostDocument> documents = elasticPostRepository.findByTitle(keyword, pageable);

        // 검색 결과가 없으면 빈 리스트 반환
        if (documents.isEmpty()) {
            return List.of();
        }

        // 검색 결과에서 Post ID만 추출
        List<Long> postIds = documents.stream()
                .map(PostDocument::getId)
                .toList();

        // 추출한 PostID 목록으로 RDB 조회
        List<Post> posts = postRepository.findAllByIdIn(postIds);

        // ES의 검색 연관도(Score) 순서를 RDB 결과에서도 유지하기 위한 Map 정렬
        Map<Long, Post> postMap = posts.stream()
                .collect(Collectors.toMap(Post::getId, post -> post));

        // PostResponseDto 리스트로 변환하여 반환
        return postIds.stream()
                .map(postMap::get)
                .filter(Objects::nonNull) // DB에서 삭제된 대상이 ES에 있을 경우 안전하게 필터링
                .map(PostResponseDto::new)
                .toList();
    }
}
