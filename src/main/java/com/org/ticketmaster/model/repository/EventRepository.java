package com.org.ticketmaster.model.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.org.ticketmaster.model.Event;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e "
            + "JOIN FETCH e.venue "
            + "JOIN FETCH e.performer "
            + "WHERE e.id = :eventId")
    Optional<Event> findEventDetailsById(@Param("eventId") Long eventId);

    @Query("SELECT e FROM Event e "
            + "JOIN FETCH e.venue "
            + "JOIN FETCH e.performer")
    List<Event> findAllWithDetails();
}