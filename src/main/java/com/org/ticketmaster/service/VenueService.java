package com.org.ticketmaster.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.org.ticketmaster.model.Venue;
import com.org.ticketmaster.model.repository.VenueRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueRepository venueRepository;

    public List<Venue> getAllVenues() {
        return venueRepository.findAll();
    }

    public Venue getVenueById(Long id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Venue not found: " + id));
    }

    public Venue createVenue(Venue venue) {
        return venueRepository.save(venue);
    }
}