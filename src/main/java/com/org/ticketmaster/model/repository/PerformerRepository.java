package com.org.ticketmaster.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.org.ticketmaster.model.Performer;

public interface PerformerRepository extends JpaRepository<Performer, Long> {
}