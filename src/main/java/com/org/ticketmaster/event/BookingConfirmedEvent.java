package com.org.ticketmaster.event;

import com.org.ticketmaster.notification.BookingConfirmationDetails;

/**
 * Published after a booking + its ticket have been committed as CONFIRMED/BOOKED.
 * Consumed by BookingConfirmationEventListener, which hands it off to SQS so the
 * (slow, third-party-dependent) work of sending the confirmation email happens
 * off the request path.
 */
public record BookingConfirmedEvent(BookingConfirmationDetails details) {
}