package com.ktb.lukas.repository;

import com.ktb.lukas.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepositoryCustom {
    List<Post> findPostsWithAuthor(Pageable pageable);
    List<Post> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
