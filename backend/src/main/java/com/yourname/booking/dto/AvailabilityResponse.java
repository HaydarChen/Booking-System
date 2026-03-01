package com.yourname.booking.dto;

public record AvailabilityResponse(
        Long inventoryId,
        int available
) {
}
