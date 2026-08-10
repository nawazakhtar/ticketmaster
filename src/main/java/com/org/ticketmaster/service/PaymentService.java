package com.org.ticketmaster.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.org.ticketmaster.dto.PaymentDetails;
import com.org.ticketmaster.dto.PaymentResult;

@Service
public class PaymentService {

    public PaymentResult processPayment(BigDecimal amount, PaymentDetails paymentDetails) {
        return PaymentResult.builder()
                .success(true)
                .transactionId(UUID.randomUUID().toString())
                .build();
    }
}