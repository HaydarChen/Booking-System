package com.yourname.booking.service;

import com.yourname.booking.dto.AvailabilityResponse;
import com.yourname.booking.dto.InventoryResponse;
import com.yourname.booking.entity.Inventory;
import com.yourname.booking.entity.InventoryType;
import com.yourname.booking.exception.NotFoundException;
import com.yourname.booking.repository.InventoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public Page<InventoryResponse> list(InventoryType type, Pageable pageable) {
        Page<Inventory> page = (type == null)
                ?   inventoryRepository.findAll(pageable)
                :   inventoryRepository.findByType(type, pageable);

        return page.map(this::toResponse);
    }

    public AvailabilityResponse getAvailability(Long id) {
        Inventory inv = inventoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Inventory not found: "+ id));
        return new AvailabilityResponse(inv.getId(), inv.getAvailable());
    }

    private InventoryResponse toResponse(Inventory inv) {
        return new InventoryResponse(
                inv.getId(),
                inv.getType(),
                inv.getItemCode(),
                inv.getTotalCapacity(),
                inv.getAvailable(),
                inv.getCreatedAt(),
                inv.getUpdatedAt()
        );
    }
}
