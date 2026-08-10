package com.org.ticketmaster.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.org.ticketmaster.model.Performer;
import com.org.ticketmaster.model.repository.PerformerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PerformerService {

    private final PerformerRepository performerRepository;

    public List<Performer> getAllPerformers() {
        return performerRepository.findAll();
    }

    public Performer getPerformerById(Long id) {
        return performerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Performer not found: " + id));
    }

    public Performer createPerformer(Performer performer) {
        return performerRepository.save(performer);
    }
}