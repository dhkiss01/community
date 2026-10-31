package com.ktb.lukas.search;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Repository
@RequiredArgsConstructor
public class PostSearchOutbox {
    private final JdbcTemplate jdbc;
    private final PostSearchProperties properties;

    public void enqueue(Long postId) {
        if (!properties.isEnabled()) return;
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("Search changes must be enqueued in the post transaction");
        }
        jdbc.update("""
                INSERT INTO post_search_outbox (post_id, updated_at) VALUES (?, CURRENT_TIMESTAMP(6))
                ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP(6)
                """, postId);
    }

    public void enqueueAll() {
        jdbc.update("""
                INSERT INTO post_search_outbox (post_id, updated_at)
                SELECT post_id, CURRENT_TIMESTAMP(6) FROM post
                ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP(6)
                """);
    }
}
