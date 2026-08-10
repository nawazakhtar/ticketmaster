package com.org.ticketmaster.service;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.org.ticketmaster.dto.EventCreateRequest;
import com.org.ticketmaster.dto.EventDetailsResponse;
import com.org.ticketmaster.dto.PerformerResponse;
import com.org.ticketmaster.dto.SeatResponse;
import com.org.ticketmaster.dto.TicketResponse;
import com.org.ticketmaster.dto.VenueResponse;
import com.org.ticketmaster.event.EventCreatedEvent;
import com.org.ticketmaster.event.EventDeletedEvent;
import com.org.ticketmaster.model.Event;
import com.org.ticketmaster.model.Performer;
import com.org.ticketmaster.model.Ticket;
import com.org.ticketmaster.model.TicketStatus;
import com.org.ticketmaster.model.Venue;
import com.org.ticketmaster.model.repository.EventRepository;
import com.org.ticketmaster.model.repository.PerformerRepository;
import com.org.ticketmaster.model.repository.TicketRepository;
import com.org.ticketmaster.model.repository.VenueRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final PerformerRepository performerRepository;
    private final TicketRepository ticketRepository;
    private final TicketLockService ticketLockService;
    private final ApplicationEventPublisher eventPublisher;

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));
    }

    public EventDetailsResponse getEventDetails(Long eventId) {
        Event event = eventRepository.findEventDetailsById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));

        List<Ticket> tickets = ticketRepository.findByEventIdWithSeat(eventId);

        List<TicketResponse> ticketResponses = tickets.stream()
                .map(ticket -> TicketResponse.builder()
                        .id(ticket.getId())
                        .price(ticket.getPrice())
                        .status(effectiveStatus(ticket))
                        .seat(SeatResponse.builder()
                                .id(ticket.getSeat().getId())
                                .section(ticket.getSeat().getSection())
                                .rowLabel(ticket.getSeat().getRowLabel())
                                .seatNumber(ticket.getSeat().getSeatNumber())
                                .build())
                        .build())
                .toList();

        return EventDetailsResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .venue(VenueResponse.builder()
                        .id(event.getVenue().getId())
                        .name(event.getVenue().getName())
                        .location(event.getVenue().getLocation())
                        .build())
                .performer(PerformerResponse.builder()
                        .id(event.getPerformer().getId())
                        .name(event.getPerformer().getName())
                        .build())
                .tickets(ticketResponses)
                .build();
    }

    private TicketStatus effectiveStatus(Ticket ticket) {
        if (ticket.getStatus() == TicketStatus.AVAILABLE && ticketLockService.isLocked(ticket.getId())) {
            return TicketStatus.RESERVED;
        }
        return ticket.getStatus();
    }

    @Transactional
    public Event createEvent(EventCreateRequest request) {
        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new IllegalArgumentException("Venue not found: " + request.getVenueId()));
        Performer performer = performerRepository.findById(request.getPerformerId())
                .orElseThrow(() -> new IllegalArgumentException("Performer not found: " + request.getPerformerId()));

        Event event = Event.builder()
                .name(request.getName())
                .description(request.getDescription())
                .eventDate(request.getEventDate())
                .venue(venue)
                .performer(performer)
                .build();

        Event saved = eventRepository.save(event);
        eventPublisher.publishEvent(new EventCreatedEvent(saved));
        return saved;
    }

    @Transactional
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new IllegalArgumentException("Event not found: " + id);
        }
        eventRepository.deleteById(id);
        eventPublisher.publishEvent(new EventDeletedEvent(id));
    }
}