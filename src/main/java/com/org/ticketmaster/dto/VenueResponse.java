package com.org.ticketmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class VenueResponse {

    private Long id;

    private String name;

    private String location;
}