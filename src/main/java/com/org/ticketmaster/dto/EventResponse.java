package com.org.ticketmaster.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class EventResponse {

    private Long id;

    private String name;

    private String description;

    private LocalDateTime eventDate;

    private VenueResponse venue;
}