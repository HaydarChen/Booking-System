package com.yourname.booking.dto;

import jakarta.validation.constraints.NotBlank;

public record PayBookingRequest(
        @NotBlank String providerRef
) {
}
