package com.ktb.lukas.repository;

import com.ktb.lukas.entity.Post;
import com.ktb.lukas.entity.QPost;
import com.ktb.lukas.entity.QUser;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QPost post = QPost.post;
    private final QUser user = QUser.user;

    public List<Post> findPostsWithAuthor(Pageable pageable) {
        return queryFactory
                .selectFrom(post)
                .join(post.author, user).fetchJoin()
                .orderBy(post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


    }
    public List<Post> findByKeyword(@Param("keyword") String keyword, Pageable pageable){
        return queryFactory
                .selectFrom(post)
                .join(post.author, user)
                .where(
                        post.title.contains(keyword)
                                .or(post.content.contains(keyword)))
                .orderBy(post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

    }

}
