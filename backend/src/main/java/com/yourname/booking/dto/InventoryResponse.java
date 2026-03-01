package com.yourname.booking.dto;

import com.yourname.booking.entity.InventoryType;

import java.time.OffsetDateTime;

public record InventoryResponse(
        Long id,
        InventoryType type,
        String itemCode,
        int totalCapacity,
        int available,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}