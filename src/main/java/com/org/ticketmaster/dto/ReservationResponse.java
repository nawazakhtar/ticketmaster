package com.org.ticketmaster.dto;

import java.time.LocalDateTime;

import com.org.ticketmaster.model.TicketStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ReservationResponse {

    private Long ticketId;

    private TicketStatus status;

    private LocalDateTime expiresAt;
}