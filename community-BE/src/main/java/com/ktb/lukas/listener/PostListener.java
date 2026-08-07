package com.ktb.lukas.listener;

import com.ktb.lukas.entity.Post;
import com.ktb.lukas.entity.PostDocument;
import com.ktb.lukas.event.PostCreateEvent;
import com.ktb.lukas.event.PostDeleteEvent;
import com.ktb.lukas.event.PostUpdateEvent;
import com.ktb.lukas.event.PostViewEvent;
import com.ktb.lukas.exception.CustomException;
import com.ktb.lukas.exception.ErrorCode;
import com.ktb.lukas.repository.ElasticPostRepository;
import com.ktb.lukas.repository.PostRepository;
import com.ktb.lukas.service.PostViewCountService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// PostViewedEvent를 수신해서 조회수 증가 로직을 담당하는 서비스에 넘긴다

@Component
@RequiredArgsConstructor
public class PostListener {

    private final ElasticPostRepository elasticPostRepository;
    private final PostViewCountService postViewCountService;
    private final PostRepository postRepository;

    @Async
    @EventListener
    public void handlePostViewEvent(PostViewEvent event) {
        postViewCountService.increase(event.getPostId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostCreateEvent(PostCreateEvent event) {
        Post post = postRepository.findById(event.getPostId())
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        PostDocument document = new PostDocument();
        document.updateFrom(post);
        elasticPostRepository.save(document);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostUpdateEvent(PostUpdateEvent event) {

        Post post = postRepository.findById(event.getPostId())
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        PostDocument document = new PostDocument();
        document.updateFrom(post);

        elasticPostRepository.save(document);

    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostDeleteEvent(PostDeleteEvent event) {
        elasticPostRepository.deleteById(event.getPostId());
    }
}

// RDB의 트랜잭션이 아직 COMMIT되지 않아 DB 상의 데이터가 변경 전 상태일 때,
// 리스너가 먼저 실행되어 RDB를 조회하면 데이터 불일치 현상이 일어날 수 있는데 그걸 해결해주기 위해서
// @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) 추가하여 이벤트는 RDB COMMIT이 성공해야만
// 이벤트를 실행할 수 있게 하였다.

