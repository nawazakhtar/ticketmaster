package com.org.ticketmaster.search;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.org.ticketmaster.dto.EventSearchPageResponse;
import com.org.ticketmaster.dto.EventSearchResponse;
import com.org.ticketmaster.model.Event;
import com.org.ticketmaster.model.repository.EventRepository;
import com.org.ticketmaster.search.document.EventDocument;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Owns the Elasticsearch index for events: keeping individual documents in sync
 * (called from EventSearchIndexListener after a DB commit) and serving search
 * queries so event lookups don't fall back to full Postgres table scans.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventSearchService {

    private final EventSearchRepository eventSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final EventRepository eventRepository;

    public void indexEvent(Event event) {
        log.info("Indexing event {} into Elasticsearch", event.getId());
        eventSearchRepository.save(toDocument(event));
    }

    public void deleteEvent(Long eventId) {
        log.info("Removing event {} from Elasticsearch", eventId);
        eventSearchRepository.deleteById(eventId);
    }

    /**
     * Rebuilds the whole index from Postgres. Useful for the initial backfill,
     * or to repair the index if it ever drifts from the source of truth.
     */
    public long reindexAll() {
        List<Event> events = eventRepository.findAllWithDetails();
        List<EventDocument> documents = events.stream().map(this::toDocument).toList();
        eventSearchRepository.saveAll(documents);
        return documents.size();
    }

    public EventSearchPageResponse search(String searchTerm, String location, LocalDate date, Pageable pageable) {
        Criteria criteria = buildCriteria(searchTerm, location, date);

        CriteriaQuery query = new CriteriaQuery(criteria);
        query.setPageable(pageable);

        SearchHits<EventDocument> searchHits = elasticsearchOperations.search(query, EventDocument.class);

        List<EventSearchResponse> results = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(this::toResponse)
                .toList();

        return EventSearchPageResponse.builder()
                .results(results)
                .totalResults(searchHits.getTotalHits())
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .build();
    }

    private Criteria buildCriteria(String searchTerm, String location, LocalDate date) {
        List<Criteria> filters = new ArrayList<>();

        if (StringUtils.hasText(searchTerm)) {
            filters.add(Criteria.where("name").matches(searchTerm)
                    .or("description").matches(searchTerm)
                    .or("venueName").matches(searchTerm)
                    .or("performerName").matches(searchTerm));
        }
        if (StringUtils.hasText(location)) {
            filters.add(Criteria.where("location").matches(location));
        }
        if (date != null) {
            filters.add(Criteria.where("eventDate").between(date.atStartOfDay(), date.atTime(23, 59, 59)));
        }

        if (filters.isEmpty()) {
            return new Criteria();
        }

        Criteria combined = filters.get(0);
        for (int i = 1; i < filters.size(); i++) {
            combined = combined.and(filters.get(i));
        }
        return combined;
    }

    private EventDocument toDocument(Event event) {
        return EventDocument.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .venueId(event.getVenue().getId())
                .venueName(event.getVenue().getName())
                .location(event.getVenue().getLocation())
                .performerId(event.getPerformer().getId())
                .performerName(event.getPerformer().getName())
                .build();
    }

    private EventSearchResponse toResponse(EventDocument document) {
        return EventSearchResponse.builder()
                .id(document.getId())
                .name(document.getName())
                .description(document.getDescription())
                .eventDate(document.getEventDate())
                .venueId(document.getVenueId())
                .venueName(document.getVenueName())
                .location(document.getLocation())
                .performerId(document.getPerformerId())
                .performerName(document.getPerformerName())
                .build();
    }
}