package com.ktb.lukas.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.search.enabled", havingValue = "true")
public class PostSearchBootstrap implements ApplicationRunner {
    private final ElasticsearchClient client;
    private final PostSearchProperties properties;
    private final PostSearchOutbox outbox;
    private final TransactionTemplate transactions;
    private volatile boolean ready;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        boolean created = false;
        if (!client.indices().exists(request -> request.index(properties.getIndex())).value()) {
            try (var settings = new ClassPathResource("elasticsearch/posts-index.json").getInputStream()) {
                try {
                    client.indices().create(request -> request.index(properties.getIndex()).withJson(settings));
                    created = true;
                } catch (ElasticsearchException e) {
                    if (!"resource_already_exists_exception".equals(e.error().type())) throw e;
                }
            }
        }
        if (created || properties.isReindexOnStartup()) {
            transactions.executeWithoutResult(status -> outbox.enqueueAll());
        }
        ready = true;
    }

    public boolean isReady() {
        return ready;
    }
}
