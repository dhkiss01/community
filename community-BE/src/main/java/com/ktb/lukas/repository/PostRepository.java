package com.ktb.lukas.repository;
import com.ktb.lukas.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom{
    @Query("SELECT p FROM Post p JOIN FETCH p.author WHERE p.id IN :ids")
    List<Post> findAllByIdIn(@Param("ids") List<Long> ids);
}
