package com.org.ticketmaster.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.org.ticketmaster.dto.SeatResponse;
import com.org.ticketmaster.dto.TicketCreateRequest;
import com.org.ticketmaster.dto.TicketResponse;
import com.org.ticketmaster.model.Ticket;
import com.org.ticketmaster.service.TicketService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@RequestBody TicketCreateRequest request) {
        Ticket ticket = ticketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(ticket));
    }

    private TicketResponse toResponse(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .price(ticket.getPrice())
                .status(ticket.getStatus())
                .seat(SeatResponse.builder()
                        .id(ticket.getSeat().getId())
                        .section(ticket.getSeat().getSection())
                        .rowLabel(ticket.getSeat().getRowLabel())
                        .seatNumber(ticket.getSeat().getSeatNumber())
                        .build())
                .build();
    }
}