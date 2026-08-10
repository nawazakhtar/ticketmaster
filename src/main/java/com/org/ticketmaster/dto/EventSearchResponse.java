package com.org.ticketmaster.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class EventSearchResponse {

    private Long id;

    private String name;

    private String description;

    private LocalDateTime eventDate;

    private Long venueId;

    private String venueName;

    private String location;

    private Long performerId;

    private String performerName;
}