package com.org.ticketmaster.controller;

import java.time.LocalDate;

import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.org.ticketmaster.dto.EventCreateRequest;
import com.org.ticketmaster.dto.EventDetailsResponse;
import com.org.ticketmaster.dto.EventResponse;
import com.org.ticketmaster.dto.EventSearchPageResponse;
import com.org.ticketmaster.dto.VenueResponse;
import com.org.ticketmaster.model.Event;
import com.org.ticketmaster.search.EventSearchService;
import com.org.ticketmaster.service.EventService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final EventSearchService eventSearchService;

    /**
     * Backed by Elasticsearch rather than Postgres so free-text search doesn't
     * turn into a full table scan. All params are optional; omitting all of
     * them returns a plain (paginated) browse of every indexed event.
     */
    @GetMapping("/search")
    public ResponseEntity<EventSearchPageResponse> searchEvents(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                eventSearchService.search(searchTerm, location, date, PageRequest.of(page, size)));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventDetailsResponse> getEventDetails(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getEventDetails(eventId));
    }

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@RequestBody EventCreateRequest request) {
        Event event = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(event));
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }

    /**
     * One-off / recovery hook to rebuild the Elasticsearch index from Postgres
     * (initial backfill, or repairing drift). Ongoing sync happens automatically
     * via EventSearchIndexListener on every create/delete.
     */
    @PostMapping("/search/reindex")
    public ResponseEntity<Long> reindex() {
        return ResponseEntity.ok(eventSearchService.reindexAll());
    }

    private EventResponse toResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .venue(VenueResponse.builder()
                        .id(event.getVenue().getId())
                        .name(event.getVenue().getName())
                        .location(event.getVenue().getLocation())
                        .build())
                .build();
    }
}