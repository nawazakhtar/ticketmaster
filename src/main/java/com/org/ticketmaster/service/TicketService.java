package com.org.ticketmaster.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.org.ticketmaster.dto.TicketCreateRequest;
import com.org.ticketmaster.model.Event;
import com.org.ticketmaster.model.Seat;
import com.org.ticketmaster.model.Ticket;
import com.org.ticketmaster.model.TicketStatus;
import com.org.ticketmaster.model.repository.EventRepository;
import com.org.ticketmaster.model.repository.SeatRepository;
import com.org.ticketmaster.model.repository.TicketRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;

    public List<Ticket> getAvailableTicketsForEvent(Long eventId) {
        return ticketRepository.findByEventIdAndStatus(eventId, TicketStatus.AVAILABLE);
    }

    public Ticket createTicket(TicketCreateRequest request) {
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + request.getEventId()));
        Seat seat = seatRepository.findById(request.getSeatId())
                .orElseThrow(() -> new IllegalArgumentException("Seat not found: " + request.getSeatId()));

        Ticket ticket = Ticket.builder()
                .event(event)
                .seat(seat)
                .price(request.getPrice())
                .status(TicketStatus.AVAILABLE)
                .build();

        return ticketRepository.save(ticket);
    }
}