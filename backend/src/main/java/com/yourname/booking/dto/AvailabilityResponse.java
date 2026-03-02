package com.yourname.booking.dto;

import java.io.Serializable;

public record AvailabilityResponse(
        Long inventoryId,
        int available
) implements Serializable {
}
