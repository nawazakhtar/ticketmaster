package com.org.ticketmaster.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.org.ticketmaster.event.BookingConfirmedEvent;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.extern.slf4j.Slf4j;

/**
 * Bridges the in-process BookingConfirmedEvent onto SQS.
 *
 * AFTER_COMMIT: only enqueue once the booking/ticket rows have actually
 * landed in Postgres - a rolled-back booking must never trigger an email.
 * fallbackExecution=true keeps this working if the service method is ever
 * invoked outside a transaction (e.g. in tests).
 *
 * The {@code Async("notificationExecutor")} annotation below (see AsyncConfig)
 * matters because AFTER_COMMIT alone still runs synchronously on the request
 * thread that called confirmBooking(), right before the HTTP response goes
 * out - the SQS round-trip would sit directly in booking latency. Async hands
 * this method off to a dedicated pool instead, so the request returns as soon
 * as the DB commit succeeds and this runs concurrently with (not before)
 * building the response.
 *
 * This class is intentionally the only place that knows about SQS; the actual
 * email send happens later, in a separate consumer
 * (BookingConfirmationQueueConsumer), which can be scaled independently of
 * the booking API and retried by SQS without holding up a request.
 */
@Component
@Slf4j
public class BookingConfirmationEventListener {

    private final SqsTemplate sqsTemplate;
    private final String queueName;

    public BookingConfirmationEventListener(
            SqsTemplate sqsTemplate,
            @Value("${app.sqs.booking-confirmed-queue}") String queueName) {
        this.sqsTemplate = sqsTemplate;
        this.queueName = queueName;
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onBookingConfirmed(BookingConfirmedEvent event) {
        BookingConfirmationDetails details = event.details();
        try {
            sqsTemplate.send(to -> to.queue(queueName).payload(details));
        } catch (Exception ex) {
            // Enqueue failures are logged, not rethrown: the booking already committed
            // and the request should still succeed. In production this should also
            // increment a metric/alert so a stuck SQS integration gets noticed - right
            // now a failure here silently means no confirmation email goes out.
            log.error("Failed to enqueue booking confirmation for booking {}", details.bookingId(), ex);
        }
    }
}