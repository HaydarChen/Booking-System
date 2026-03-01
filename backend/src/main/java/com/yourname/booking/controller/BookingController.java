package com.yourname.booking.controller;

import com.yourname.booking.dto.BookingResponse;
import com.yourname.booking.dto.CreateBookingRequest;
import com.yourname.booking.dto.PayBookingRequest;
import com.yourname.booking.dto.PayBookingResponse;
import com.yourname.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingResponse create(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CreateBookingRequest request
    ) {
        return bookingService.createBooking(request, idempotencyKey);
    }

    @GetMapping("/{id}")
    public BookingResponse get(@PathVariable UUID id) {
        return bookingService.get(id);
    }

    @PostMapping("/{id}/pay")
    public PayBookingResponse pay(
            @PathVariable("id") UUID bookingId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody PayBookingRequest request
    ) {
        return bookingService.pay(bookingId, idempotencyKey, request.providerRef());
    }
}
