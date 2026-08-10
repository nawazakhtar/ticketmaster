package com.org.ticketmaster.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class EventSearchPageResponse {

    private List<EventSearchResponse> results;

    private long totalResults;

    private int page;

    private int size;
}