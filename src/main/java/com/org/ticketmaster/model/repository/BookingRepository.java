package com.org.ticketmaster.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.org.ticketmaster.model.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}