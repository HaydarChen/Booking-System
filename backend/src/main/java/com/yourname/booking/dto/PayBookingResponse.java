package com.yourname.booking.dto;

public record PayBookingResponse(
        BookingResponse booking,
        PaymentResponse payment
) {
}
