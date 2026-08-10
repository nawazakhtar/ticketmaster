package com.org.ticketmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PaymentResult {

    private boolean success;

    private String transactionId;
}