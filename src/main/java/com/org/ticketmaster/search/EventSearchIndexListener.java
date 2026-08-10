package com.org.ticketmaster.search;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.org.ticketmaster.event.EventCreatedEvent;
import com.org.ticketmaster.event.EventDeletedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Reacts to Event create/delete domain events published from EventService and
 * mirrors the change into Elasticsearch. This is the "observer" that keeps the
 * search index up to date without EventService knowing search exists at all.
 *
 * Listens AFTER_COMMIT so the index is only updated once the DB change has
 * actually landed - a rollback never leaves a stale/phantom document behind.
 * fallbackExecution=true keeps this working even if a caller invokes the
 * service outside of a transaction (e.g. in tests).
 *
 * Indexing failures are caught and logged rather than rethrown: a hiccup
 * talking to Elasticsearch should never fail the request that already
 * committed to Postgres. If this were a higher-stakes system, a failed write
 * here would instead go through an outbox/retry table so the index is
 * eventually made consistent rather than silently skipped.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventSearchIndexListener {

    private final EventSearchService eventSearchService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onEventCreated(EventCreatedEvent event) {
        try {
            eventSearchService.indexEvent(event.event());
        } catch (Exception ex) {
            log.error("Failed to index event {} into Elasticsearch", event.event().getId(), ex);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onEventDeleted(EventDeletedEvent event) {
        try {
            eventSearchService.deleteEvent(event.eventId());
        } catch (Exception ex) {
            log.error("Failed to remove event {} from Elasticsearch", event.eventId(), ex);
        }
    }
}