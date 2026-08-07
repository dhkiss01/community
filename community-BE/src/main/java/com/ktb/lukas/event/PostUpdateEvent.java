package com.ktb.lukas.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PostUpdateEvent {
    private final Long postId;
}
