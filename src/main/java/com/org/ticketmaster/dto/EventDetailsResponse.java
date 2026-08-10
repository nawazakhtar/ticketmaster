package com.org.ticketmaster.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class EventDetailsResponse {

    private Long id;

    private String name;

    private String description;

    private LocalDateTime eventDate;

    private VenueResponse venue;

    private PerformerResponse performer;

    private List<TicketResponse> tickets;
}