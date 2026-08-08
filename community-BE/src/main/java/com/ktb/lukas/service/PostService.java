package com.ktb.lukas.service;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ktb.lukas.dto.PostRequestDto;
import com.ktb.lukas.dto.PostResponseDto;
import com.ktb.lukas.entity.Post;
import com.ktb.lukas.entity.User;
import com.ktb.lukas.event.PostViewedEvent;
import com.ktb.lukas.exception.CustomException;
import com.ktb.lukas.exception.ErrorCode;
import com.ktb.lukas.repository.PostRepository;
import com.ktb.lukas.repository.UserRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public PostResponseDto createPost(Long userId, PostRequestDto request) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Post post = new Post(
                request.getTitle(),
                request.getContent(),
                request.getImage(),
                author
        );

        Post savedPost = postRepository.save(post);
        return new PostResponseDto(savedPost);
    }
    // 게시글 단건 조회
    @Transactional(readOnly = true)
    public PostResponseDto getPost(Long userId, Long postId) {
        Post post = findPost(postId);
        publisher.publishEvent(
                new PostViewedEvent(userId, postId)  // postView 이벤트 발행함 스프링 이벤트 시스템이 리스너로 보냄
        );
        return new PostResponseDto(post);
    }
    // 게시글 전부 조회
    // fetch join을 통해 N+1 문제 해결
    @Transactional(readOnly = true)
    public List<PostResponseDto> getPosts(int page) {
        Pageable pageable = PageRequest.of(page, 10);

        return postRepository.findPostsWithAuthor(pageable)
                .stream()
                .map(PostResponseDto::new)
                .toList();
    }
    // 게시글 검색
    @Transactional
    public List<PostResponseDto> getPostsBySearch(String keyword, int page) {
        Pageable pageable = PageRequest.of(page, 10);

        return postRepository.findByKeyword(keyword, pageable)
                .stream()
                .map(PostResponseDto::new)
                .toList();
    }

    // 게시글 수정
    @Transactional
    public PostResponseDto updatePost(Long userId, Long postId, PostRequestDto request) {

        Post post = findPost(postId);

        if (!post.getAuthor().getId().equals(userId)) {
            throw new CustomException(ErrorCode.POST_UPDATE_FORBIDDEN); // 게시글 작성자가 아니면 수정할 수 없음
        }

        post.changeTitle(request.getTitle());
        post.changeContent(request.getContent());

        return new PostResponseDto(post);
    }
    // 게시글 삭제
    @Transactional
    public void deletePost(Long userId, Long postId) {

        Post post = findPost(postId);

        if (!post.getAuthor().getId().equals(userId)) {
            throw new CustomException(ErrorCode.POST_DELETE_FORBIDDEN); // 게시글 작성자가 아니면 삭제할 수 없음
        }

        postRepository.delete(post);
    }

    private Post findPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }
}