package com.org.ticketmaster.event;

/**
 * Published after an Event has been removed from the database.
 * Consumed by search-index listeners to remove the corresponding document
 * from Elasticsearch.
 */
public record EventDeletedEvent(Long eventId) {
}