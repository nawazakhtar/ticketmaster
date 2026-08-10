package com.org.ticketmaster.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentDetails {

    private String cardNumber;

    private String cardHolderName;

    private String expiry;

    private String cvv;
}