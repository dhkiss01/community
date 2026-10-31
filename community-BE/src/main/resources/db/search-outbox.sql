CREATE TABLE IF NOT EXISTS post_search_outbox (
    post_id BIGINT NOT NULL PRIMARY KEY,
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    KEY idx_post_search_outbox_updated (updated_at, post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
