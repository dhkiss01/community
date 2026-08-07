package com.ktb.lukas.service;

import com.ktb.lukas.entity.Post;
import com.ktb.lukas.exception.CustomException;
import com.ktb.lukas.exception.ErrorCode;
import com.ktb.lukas.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


// 리스너에게 위임받아 Post 엔티티에 있는 increaseViewCount 메서드를 실행시켜 Viewcount를 ++ 해주는 서비스
// 만약 Post가 없다면 예외처리함

@Service
@RequiredArgsConstructor
public class PostViewCountService {

    private final PostRepository postRepository;

    @Transactional
    public void increase(Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

            post.increaseViewCount();

    }
}

