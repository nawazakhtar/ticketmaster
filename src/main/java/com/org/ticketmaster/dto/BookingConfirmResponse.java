package com.org.ticketmaster.dto;

import com.org.ticketmaster.model.TicketStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BookingConfirmResponse {

    private Long bookingId;

    private Long ticketId;

    private TicketStatus status;

    private String transactionId;

    private EventResponse event;
}