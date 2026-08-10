package com.org.ticketmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SeatResponse {

    private Long id;

    private String section;

    private String rowLabel;

    private String seatNumber;
}