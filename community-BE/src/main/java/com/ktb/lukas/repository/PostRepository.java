package com.ktb.lukas.repository;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ktb.lukas.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("""
    SELECT p
    FROM Post p
    JOIN FETCH p.author
    ORDER BY p.id DESC
""")
    List<Post> findPostsWithAuthor(Pageable pageable);
    @Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword%")
    List<Post> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

}
