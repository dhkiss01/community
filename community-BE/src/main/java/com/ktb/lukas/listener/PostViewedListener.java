package com.ktb.lukas.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ktb.lukas.event.PostViewedEvent;
import com.ktb.lukas.service.PostViewCountService;

import lombok.RequiredArgsConstructor;

// PostViewedEvent를 수신해서 조회수 증가 로직을 담당하는 서비스에 넘긴다

@Component
@RequiredArgsConstructor
public class PostViewedListener {

    private final PostViewCountService postViewCountService;

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(PostViewedEvent event) {
        postViewCountService.increase(event.getPostId());
    }
}
