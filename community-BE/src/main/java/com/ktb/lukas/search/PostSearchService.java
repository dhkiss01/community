package com.ktb.lukas.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import com.ktb.lukas.exception.CustomException;
import com.ktb.lukas.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostSearchService {
    private final PostSearchProperties properties;
    private final ElasticsearchClient client;

    public boolean isEnabled() {
        return properties.isEnabled();
    }

    public List<Long> searchIds(String keyword, Pageable pageable, String sort) {
        String normalized = keyword.strip();
        try {
            return client.search(request -> request.index(properties.getIndex())
                    .from(Math.toIntExact(pageable.getOffset())).size(pageable.getPageSize())
                    .source(source -> source.fetch(false))
                    .query(query -> query.multiMatch(match -> match.query(normalized)
                            .fields("title^2", "content").type(TextQueryType.CrossFields)
                            .operator(Operator.And).analyzer("baseball_search")))
                    .sort(order -> "recent".equals(sort)
                            ? order.field(field -> field.field("createdAt").order(SortOrder.Desc))
                            : order.score(score -> score.order(SortOrder.Desc)))
                    .sort(order -> order.field(field -> field.field("createdAt").order(SortOrder.Desc)))
                    .sort(order -> order.field(field -> field.field("id").order(SortOrder.Desc))), Void.class)
                    .hits().hits().stream().map(hit -> Long.valueOf(hit.id())).toList();
        } catch (IOException | ElasticsearchException e) {
            throw new CustomException(ErrorCode.SEARCH_UNAVAILABLE);
        }
    }
}
