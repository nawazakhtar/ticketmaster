package com.org.ticketmaster.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.org.ticketmaster.model.Venue;

public interface VenueRepository extends JpaRepository<Venue, Long> {
}