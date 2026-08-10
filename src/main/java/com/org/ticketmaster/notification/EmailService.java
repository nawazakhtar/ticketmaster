package com.org.ticketmaster.notification;

public interface EmailService {

    void sendBookingConfirmation(BookingConfirmationDetails details);
}