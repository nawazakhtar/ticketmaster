package com.org.ticketmaster.dto;

import java.math.BigDecimal;

import com.org.ticketmaster.model.TicketStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TicketResponse {

    private Long id;

    private SeatResponse seat;

    private BigDecimal price;

    private TicketStatus status;
}