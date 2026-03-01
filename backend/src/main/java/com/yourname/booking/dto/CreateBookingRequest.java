package com.yourname.booking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateBookingRequest(
        @NotNull
        Long inventoryId,

        @Min(1)
        int quantity
) {
}
