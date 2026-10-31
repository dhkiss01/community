package com.ktb.lukas.repository;

import com.ktb.lukas.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Collection;
import java.util.Optional;

public interface PostRepositoryCustom {
    default List<Post> findPostsWithAuthor(Pageable pageable) { return findPostsWithAuthor(pageable, null); }
    List<Post> findPostsWithAuthor(Pageable pageable, com.ktb.lukas.entity.PostCategory category);
    default List<Post> findByKeyword(String keyword, Pageable pageable) { return findByKeyword(keyword, pageable, null); }
    List<Post> findByKeyword(String keyword, Pageable pageable, com.ktb.lukas.entity.PostCategory category);
    List<Post> findAllWithAuthorByIdIn(Collection<Long> ids);
    Optional<Post> findWithAuthorById(Long id);
}
