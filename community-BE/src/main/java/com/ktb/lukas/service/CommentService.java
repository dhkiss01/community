    package com.ktb.lukas.service;

    import com.ktb.lukas.dto.*;
    import com.ktb.lukas.entity.Comment;
    import com.ktb.lukas.entity.Post;
    import com.ktb.lukas.entity.User;
    import com.ktb.lukas.exception.CustomException;
    import com.ktb.lukas.exception.ErrorCode;
    import com.ktb.lukas.repository.CommentRepository;
    import com.ktb.lukas.repository.PostRepository;
    import com.ktb.lukas.repository.UserRepository;
    import org.springframework.transaction.annotation.Transactional;
    import lombok.RequiredArgsConstructor;
    import org.springframework.stereotype.Service;

    import java.util.List;
    import java.util.stream.Collectors;

    @RequiredArgsConstructor
    @Service
    public class CommentService {
        private final PostRepository postRepository;
        private final CommentRepository commentRepository;
        private final UserRepository userRepository;

        @Transactional
        public CommentResponse createComment(Long userId, Long postId, CommentRequest request) {
            User commentAuthor = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            Post post = postRepository.findLockedById(postId)
                    .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

            Comment comment = new Comment(
                    request.getComment(),
                    commentAuthor,
                    post
            );

            Comment savedcomment = commentRepository.save(comment);
            post.increaseCommentCount();
            return new CommentResponse(savedcomment);

        }

        @Transactional(readOnly = true)
        public List<CommentResponse> getComments(Long postId) {
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

            List<Comment> comments = commentRepository.findByPost(post);
            return comments.stream()
                    .map(CommentResponse::new).toList();
        }

        @Transactional
        public CommentResponse updateComment(
                Long userId,
                Long postId,
                Long commentId,
                CommentRequest request
        ) {
            Comment comment  = findComment(commentId);
            if (!comment.getPost().getId().equals(postId)) throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);

            if (!comment.getUser().getId().equals(userId)) {
                throw new CustomException(ErrorCode.COMMENT_UPDATE_FORBIDDEN);
            }

            comment.changeComment(request.getComment());
            return new CommentResponse(comment);
        }


        @Transactional
        public void deleteComment(Long userId, Long postId, Long commentId) {

            Post post = postRepository.findLockedById(postId)
                    .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
            Comment comment  = findComment(commentId);
            if (!comment.getPost().getId().equals(postId)) throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);

            if (!comment.getUser().getId().equals(userId)) {
                throw new CustomException(ErrorCode.COMMENT_DELETE_FORBIDDEN);
            }
            post.decreaseCommentCount();
            commentRepository.delete(comment);
        }

        private Comment findComment(Long commentId) {
            return commentRepository.findById(commentId)
                    .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        }
    }
