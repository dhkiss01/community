package com.ktb.lukas.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostViewedEvent {
    private Long viewerId;
    private Long postId;

}


// Event class와
// 이벤트를 발생시키는 Event publisher
// 이벤트를 받아들이는 Event listener