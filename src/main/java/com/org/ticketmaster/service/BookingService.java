package com.org.ticketmaster.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.org.ticketmaster.dto.BookingConfirmRequest;
import com.org.ticketmaster.dto.BookingConfirmResponse;
import com.org.ticketmaster.dto.BookingRequest;
import com.org.ticketmaster.dto.EventResponse;
import com.org.ticketmaster.dto.PaymentResult;
import com.org.ticketmaster.dto.ReservationResponse;
import com.org.ticketmaster.dto.VenueResponse;
import com.org.ticketmaster.event.BookingConfirmedEvent;
import com.org.ticketmaster.model.Booking;
import com.org.ticketmaster.model.BookingStatus;
import com.org.ticketmaster.model.Event;
import com.org.ticketmaster.model.Ticket;
import com.org.ticketmaster.model.TicketStatus;
import com.org.ticketmaster.model.User;
import com.org.ticketmaster.model.repository.BookingRepository;
import com.org.ticketmaster.model.repository.TicketRepository;
import com.org.ticketmaster.model.repository.UserRepository;
import com.org.ticketmaster.notification.BookingConfirmationDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketLockService ticketLockService;
    private final PaymentService paymentService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Booking bookTickets(BookingRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getUserId()));

        List<Ticket> tickets = ticketRepository.findAllById(request.getTicketIds());
        if (tickets.size() != request.getTicketIds().size()) {
            throw new IllegalArgumentException("One or more tickets do not exist");
        }

        for (Ticket ticket : tickets) {
            if (ticket.getStatus() != TicketStatus.AVAILABLE) {
                throw new IllegalStateException("Ticket " + ticket.getId() + " is not available");
            }
        }

        BigDecimal totalAmount = tickets.stream()
                .map(Ticket::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Booking booking = Booking.builder()
                .user(user)
                .totalAmount(totalAmount)
                .bookingTime(LocalDateTime.now())
                .status(BookingStatus.CONFIRMED)
                .build();
        booking = bookingRepository.save(booking);

        for (Ticket ticket : tickets) {
            ticket.setStatus(TicketStatus.BOOKED);
            ticket.setBooking(booking);
        }
        ticketRepository.saveAll(tickets);
        booking.setTickets(tickets);

        return booking;
    }

    public ReservationResponse reserveTicket(Long ticketId, Long userId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));

        if (ticket.getStatus() != TicketStatus.AVAILABLE) {
            throw new IllegalStateException("Ticket " + ticketId + " is not available");
        }

        boolean locked = ticketLockService.tryLock(ticketId, userId);
        if (!locked) {
            throw new IllegalStateException("Ticket " + ticketId + " is already reserved");
        }

        return ReservationResponse.builder()
                .ticketId(ticketId)
                .status(TicketStatus.RESERVED)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();
    }

    @Transactional
    public BookingConfirmResponse confirmBooking(BookingConfirmRequest request) {
        Long ticketId = request.getTicketId();

        Optional<Long> lockOwner = ticketLockService.getLockOwner(ticketId);
        if (lockOwner.isEmpty()) {
            throw new IllegalStateException("Reservation expired or not found for ticket " + ticketId);
        }
        if (!lockOwner.get().equals(request.getUserId())) {
            throw new IllegalStateException("Ticket " + ticketId + " is reserved by another user");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getUserId()));

        PaymentResult paymentResult = paymentService.processPayment(ticket.getPrice(), request.getPaymentDetails());

        if (!paymentResult.isSuccess()) {
            return BookingConfirmResponse.builder()
                    .ticketId(ticketId)
                    .status(TicketStatus.RESERVED)
                    .build();
        }

        Booking booking = Booking.builder()
                .user(user)
                .totalAmount(ticket.getPrice())
                .bookingTime(LocalDateTime.now())
                .status(BookingStatus.CONFIRMED)
                .build();
        booking = bookingRepository.save(booking);

        ticket.setStatus(TicketStatus.BOOKED);
        ticket.setBooking(booking);
        ticketRepository.save(ticket);

        ticketLockService.releaseLock(ticketId);

        Event event = ticket.getEvent();

        eventPublisher.publishEvent(new BookingConfirmedEvent(BookingConfirmationDetails.builder()
                .bookingId(booking.getId())
                .transactionId(paymentResult.getTransactionId())
                .bookingTime(booking.getBookingTime())
                .totalAmount(booking.getTotalAmount())
                .userId(user.getId())
                .userEmail(user.getEmail())
                .userName(user.getName())
                .ticketId(ticket.getId())
                .seatSection(ticket.getSeat().getSection())
                .seatRow(ticket.getSeat().getRowLabel())
                .seatNumber(ticket.getSeat().getSeatNumber())
                .eventId(event.getId())
                .eventName(event.getName())
                .eventDate(event.getEventDate())
                .venueName(event.getVenue().getName())
                .venueLocation(event.getVenue().getLocation())
                .build()));

        return BookingConfirmResponse.builder()
                .bookingId(booking.getId())
                .ticketId(ticketId)
                .status(TicketStatus.BOOKED)
                .transactionId(paymentResult.getTransactionId())
                .event(EventResponse.builder()
                        .id(event.getId())
                        .name(event.getName())
                        .description(event.getDescription())
                        .venue(VenueResponse.builder()
                                .id(event.getVenue().getId())
                                .name(event.getVenue().getName())
                                .location(event.getVenue().getLocation())
                                .build())
                        .build())
                .build();
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + id));
    }

    public List<Booking> getBookingsForUser(Long userId) {
        return bookingRepository.findAll().stream()
                .filter(b -> b.getUser().getId().equals(userId))
                .toList();
    }
}