package com.yourname.booking.controller;

import com.yourname.booking.dto.BookingResponse;
import com.yourname.booking.dto.CreateBookingRequest;
import com.yourname.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingResponse create(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateBookingRequest request
    ) {
        return bookingService.createBooking(request, idempotencyKey);
    }
}
