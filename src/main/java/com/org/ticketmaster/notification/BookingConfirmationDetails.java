package com.org.ticketmaster.notification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;

/**
 * Flat, self-contained payload for the "booking confirmed" notification.
 * This - not the JPA entities - is what gets published as the domain event
 * and serialized onto SQS, so the notification path never touches Hibernate
 * proxies/lazy associations once the request thread that built it is gone.
 */
@Builder
public record BookingConfirmationDetails(
        Long bookingId,
        String transactionId,
        LocalDateTime bookingTime,
        BigDecimal totalAmount,
        Long userId,
        String userEmail,
        String userName,
        Long ticketId,
        String seatSection,
        String seatRow,
        String seatNumber,
        Long eventId,
        String eventName,
        LocalDateTime eventDate,
        String venueName,
        String venueLocation) {
}