package com.org.ticketmaster.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.org.ticketmaster.model.Ticket;
import com.org.ticketmaster.model.TicketStatus;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByEventIdAndStatus(Long eventId, TicketStatus status);

    @Query("SELECT t FROM Ticket t JOIN FETCH t.seat WHERE t.event.id = :eventId")
    List<Ticket> findByEventIdWithSeat(@Param("eventId") Long eventId);
}