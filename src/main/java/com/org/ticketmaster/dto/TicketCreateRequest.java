package com.org.ticketmaster.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketCreateRequest {

    private Long eventId;

    private Long seatId;

    private BigDecimal price;
}