package com.ktb.lukas.repository;

import com.ktb.lukas.config.QuerydslConfig;
import com.ktb.lukas.dto.CommentResponse;
import com.ktb.lukas.dto.PostResponseDto;
import com.ktb.lukas.entity.Comment;
import com.ktb.lukas.entity.Post;
import com.ktb.lukas.entity.User;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@Import(QuerydslConfig.class)
@ActiveProfiles("test")
class QueryCountTest {
    @Autowired EntityManager em;
    @Autowired PostRepository posts;
    @Autowired CommentRepository comments;

    @Test
    void listsSearchAndCommentsFetchAuthorsInOneQuery() {
        Post first = null;
        for (int i = 0; i < 12; i++) {
            User u = new User("fan" + i + "@example.com", "encoded", "팬" + i, null);
            em.persist(u);
            Post p = new Post("한화 경기 " + i, "직관 후기", null, u);
            em.persist(p);
            if (first == null) first = p;
            em.persist(new Comment("응원합니다", u, first));
        }
        em.flush();
        long firstId = first.getId();
        em.clear();
        var stats = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
        stats.clear();
        assertThat(posts.findPostsWithAuthor(PageRequest.of(0, 10)).stream().map(PostResponseDto::new).toList()).hasSize(10);
        assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
        em.clear(); stats.clear();
        assertThat(posts.findByKeyword("한화", PageRequest.of(0, 10)).stream().map(PostResponseDto::new).toList()).hasSize(10);
        assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
        em.clear(); stats.clear();
        var reference = em.getReference(Post.class, firstId);
        assertThat(comments.findByPost(reference).stream().map(CommentResponse::new).toList()).hasSize(12);
        assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
    }
}
