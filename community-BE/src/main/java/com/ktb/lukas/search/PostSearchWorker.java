package com.ktb.lukas.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.search.enabled", havingValue = "true")
public class PostSearchWorker {
    private final JdbcTemplate jdbc;
    private final ElasticsearchClient client;
    private final PostSearchProperties properties;
    private final PostSearchBootstrap bootstrap;
    private final TransactionTemplate transactions;
    private long retryAfter;

    @Scheduled(fixedDelayString = "${app.search.poll-interval-ms:1000}")
    public void poll() {
        if (!bootstrap.isReady() || System.currentTimeMillis() < retryAfter) return;
        try {
            transactions.executeWithoutResult(status -> synchronizeBatch());
        } catch (Exception e) {
            retryAfter = System.currentTimeMillis() + 5000;
            log.warn("Search synchronization failed; pending changes retained ({})", e.getClass().getSimpleName());
        }
    }

    private void synchronizeBatch() {
        int batchSize = Math.max(1, Math.min(properties.getBatchSize(), 500));
        List<Long> ids = jdbc.queryForList("""
                SELECT post_id FROM post_search_outbox ORDER BY updated_at, post_id
                LIMIT ? FOR UPDATE SKIP LOCKED
                """, Long.class, batchSize);
        if (ids.isEmpty()) return;
        String placeholders = ids.stream().map(id -> "?").collect(Collectors.joining(","));
        Map<Long, Map<String, Object>> documents = new HashMap<>();
        jdbc.query("SELECT post_id, post_title, post_content, create_at, category FROM post WHERE post_id IN (" + placeholders + ")",
                result -> {
                    long id = result.getLong("post_id");
                    Timestamp createdAt = result.getTimestamp("create_at");
                    documents.put(id, Map.of("id", id, "title", Objects.toString(result.getString("post_title"), ""),
                            "content", Objects.toString(result.getString("post_content"), ""),
                            "category", result.getString("category"),
                            "createdAt", createdAt.toInstant().toString()));
                }, ids.toArray());
        BulkRequest.Builder request = new BulkRequest.Builder().index(properties.getIndex());
        for (Long id : ids) {
            if (documents.containsKey(id)) {
                request.operations(operation -> operation.index(index -> index.id(id.toString()).document(documents.get(id))));
            } else {
                request.operations(operation -> operation.delete(delete -> delete.id(id.toString())));
            }
        }
        try {
            if (client.bulk(request.build()).errors()) {
                throw new IllegalStateException("Elasticsearch rejected a search index batch");
            }
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Elasticsearch index batch could not be sent", e);
        }
        jdbc.update("DELETE FROM post_search_outbox WHERE post_id IN (" + placeholders + ")", ids.toArray());
    }
}
