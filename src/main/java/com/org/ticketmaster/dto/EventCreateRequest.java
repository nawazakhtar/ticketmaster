package com.org.ticketmaster.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventCreateRequest {

    private String name;

    private String description;

    private LocalDateTime eventDate;

    private Long venueId;

    private Long performerId;
}