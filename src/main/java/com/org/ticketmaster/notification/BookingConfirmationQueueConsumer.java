package com.org.ticketmaster.notification;

import org.springframework.stereotype.Component;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Polls the booking-confirmed queue and sends the confirmation email.
 *
 * This is the piece that scales independently of the booking API: run more
 * instances/pods of this consumer (or split it into its own deployable
 * entirely) to keep up with email provider throughput, without the booking
 * write path ever waiting on it. If sendBookingConfirmation throws, the
 * message becomes visible again after the queue's visibility timeout and is
 * retried automatically; configure a redrive policy/DLQ on the queue so a
 * message that keeps failing doesn't loop forever.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookingConfirmationQueueConsumer {

    private final EmailService emailService;

    @SqsListener("${app.sqs.booking-confirmed-queue}")
    public void handle(BookingConfirmationDetails details) {
        log.info("Processing booking confirmation for booking {}", details.bookingId());
        emailService.sendBookingConfirmation(details);
    }
}