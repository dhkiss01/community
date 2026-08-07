package com.ktb.lukas.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PostDeleteEvent {
    private final Long postId;
}
