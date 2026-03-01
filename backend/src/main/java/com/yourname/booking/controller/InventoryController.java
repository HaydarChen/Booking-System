package com.yourname.booking.controller;

import com.yourname.booking.dto.AvailabilityResponse;
import com.yourname.booking.dto.InventoryResponse;
import com.yourname.booking.entity.InventoryType;
import com.yourname.booking.service.InventoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventories")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // GET /api/inventories?type=FLIGHT&page=0&size=20
    @GetMapping
    public Page<InventoryResponse> list(
            @RequestParam(required = false) InventoryType type,
            Pageable pageable
    ) {
        return inventoryService.list(type, pageable);
    }

    // GET /api/inventories/{id}/availability
    @GetMapping("/{id}/availability")
    public AvailabilityResponse availability(@PathVariable Long id) {
        return inventoryService.getAvailability(id);
    }
}
