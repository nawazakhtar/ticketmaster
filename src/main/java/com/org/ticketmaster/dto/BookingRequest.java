package com.org.ticketmaster.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingRequest {

    private Long userId;

    private List<Long> ticketIds;
}