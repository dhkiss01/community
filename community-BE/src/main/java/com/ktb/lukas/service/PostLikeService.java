package com.ktb.lukas.service;

import com.ktb.lukas.entity.Post;
import com.ktb.lukas.entity.Postlike;
import com.ktb.lukas.entity.User;
import com.ktb.lukas.exception.CustomException;
import com.ktb.lukas.exception.ErrorCode;
import com.ktb.lukas.repository.LikeRepository;
import com.ktb.lukas.repository.PostRepository;
import com.ktb.lukas.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class PostLikeService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;

    public int insertLike(Long postId, Long userId) {
        Post post = postRepository.findLockedById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (likeRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new CustomException(ErrorCode.POST_ALREADY_LIKED);
        }

        Postlike like = Postlike.builder()
                .user(user)
                .post(post)
                .build();
        likeRepository.save(like);
        post.increaseLikeCount();
        return post.getLikeCount();
    }

    public int deleteLike(Long postId, Long userId) {
        Post post = postRepository.findLockedById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        Postlike like = likeRepository.findByPostIdAndUserId(postId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_LIKE_NOT_FOUND));

        likeRepository.delete(like);
        post.decreaseLikeCount();
        return post.getLikeCount();
    }

}

