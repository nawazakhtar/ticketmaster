package com.org.ticketmaster.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.org.ticketmaster.search.document.EventDocument;

public interface EventSearchRepository extends ElasticsearchRepository<EventDocument, Long> {
}