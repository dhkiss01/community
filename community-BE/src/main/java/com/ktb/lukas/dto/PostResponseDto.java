package com.ktb.lukas.dto;

import com.ktb.lukas.entity.Post;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class PostResponseDto {
    private Long id;
    private String title;
    private com.ktb.lukas.entity.PostCategory category;
    private String content;
    private String image;

    private Long userId;
    private String nickname;
    private String profileImage;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PostResponseDto(Post post) {
        this.id = post.getId();
        this.category = post.getCategory();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.image = post.getImage();

        if (post.getAuthor() != null) {
            this.userId = post.getAuthor().getId();
            this.nickname = post.getAuthor().getNickname();
            this.profileImage = post.getAuthor().getImage();
        }

        this.viewCount = post.getViewCount();
        this.likeCount = post.getLikeCount();
        this.commentCount = post.getCommentCount();

        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
    }
}
