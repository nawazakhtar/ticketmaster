package com.org.ticketmaster.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingConfirmRequest {

    private Long ticketId;

    private Long userId;

    private PaymentDetails paymentDetails;
}