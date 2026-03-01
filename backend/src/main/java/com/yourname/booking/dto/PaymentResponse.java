package com.yourname.booking.dto;

import com.yourname.booking.entity.PaymentStatus;

import java.time.OffsetDateTime;

public record PaymentResponse(
        Long id,
        String providerRef,
        PaymentStatus status,
        OffsetDateTime paidAt
) {
}
