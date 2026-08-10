package com.org.ticketmaster.event;

import com.org.ticketmaster.model.Event;

/**
 * Published after an {@link Event} has been committed to the database.
 * Consumed by search-index listeners to keep Elasticsearch in sync,
 * decoupling the write path from any particular downstream consumer.
 */
public record EventCreatedEvent(Event event) {
}