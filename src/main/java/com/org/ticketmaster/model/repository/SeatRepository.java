package com.org.ticketmaster.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.org.ticketmaster.model.Seat;

public interface SeatRepository extends JpaRepository<Seat, Long> {
}