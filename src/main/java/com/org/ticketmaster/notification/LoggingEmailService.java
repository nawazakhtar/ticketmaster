package com.org.ticketmaster.notification;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Placeholder EmailService, same spirit as PaymentService's stubbed
 * processPayment: logs what would be sent instead of calling a real
 * provider. Swap this out for an SES/SendGrid-backed implementation
 * without touching BookingConfirmationQueueConsumer.
 */
@Service
@Slf4j
public class LoggingEmailService implements EmailService {

    @Override
    public void sendBookingConfirmation(BookingConfirmationDetails details) {
        log.info("Sending booking confirmation email to {} for booking {} ({} at {}, {})",
                details.userEmail(), details.bookingId(), details.eventName(),
                details.venueName(), details.venueLocation());
    }
}