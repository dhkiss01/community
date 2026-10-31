package com.ktb.lukas.repository;

import com.ktb.lukas.entity.Comment;
import com.ktb.lukas.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;


public interface CommentRepository extends JpaRepository<Comment, Long> {
    @EntityGraph(attributePaths = "user")
    List<Comment> findByPost(Post post);
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("delete from Comment c where c.post.id = :postId")
    void deleteAllByPostId(@org.springframework.data.repository.query.Param("postId") Long postId);
}
