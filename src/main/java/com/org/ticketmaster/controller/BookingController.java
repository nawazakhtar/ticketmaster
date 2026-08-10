package com.org.ticketmaster.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.org.ticketmaster.dto.BookingConfirmRequest;
import com.org.ticketmaster.dto.BookingConfirmResponse;
import com.org.ticketmaster.dto.ReservationResponse;
import com.org.ticketmaster.service.BookingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/reserve")
    public ResponseEntity<ReservationResponse> reserve(@RequestParam Long ticketId, @RequestParam Long userId) {
        return ResponseEntity.ok(bookingService.reserveTicket(ticketId, userId));
    }

    @PostMapping("/confirm")
    public ResponseEntity<BookingConfirmResponse> confirm(@RequestBody BookingConfirmRequest request) {
        return ResponseEntity.ok(bookingService.confirmBooking(request));
    }
}