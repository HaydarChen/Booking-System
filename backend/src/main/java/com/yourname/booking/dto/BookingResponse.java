package com.yourname.booking.dto;

import com.yourname.booking.entity.BookingStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BookingResponse(
    UUID id,
    Long InventoryId,
    int quantity,
    BookingStatus status,
    OffsetDateTime expiredAt
) {
}
