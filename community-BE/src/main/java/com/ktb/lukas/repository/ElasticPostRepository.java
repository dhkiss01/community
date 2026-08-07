package com.ktb.lukas.repository;

import com.ktb.lukas.entity.PostDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ElasticPostRepository extends ElasticsearchRepository<PostDocument, Long> {
    List<PostDocument> findByTitle(String keyword, Pageable page);
}
