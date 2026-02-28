package com.yourname.booking.config;

import com.yourname.booking.entity.Inventory;
import com.yourname.booking.entity.InventoryType;
import com.yourname.booking.repository.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedInventories(InventoryRepository inventoryRepository) {
        return args -> {
            if (inventoryRepository.count() > 0) return;

            inventoryRepository.save(make(InventoryType.FLIGHT, "GA-100|2026-03-01|CGK-DPS", 10));
            inventoryRepository.save(make(InventoryType.FLIGHT, "SQ-951|2026-03-01|CGK-SIN", 5));
            inventoryRepository.save(make(InventoryType.HOTEL, "HILTON-BALI|DELUXE|2026-03-01", 8));
            inventoryRepository.save(make(InventoryType.HOTEL, "AYANA-BALI|VILLA|2026-03-01", 2));
        };
    }

    private Inventory make(InventoryType type, String itemCode, int capacity) {
        Inventory inv = new Inventory();
        inv.setType(type);
        inv.setItemCode(itemCode);
        inv.setTotalCapacity(capacity);
        inv.setAvailable(capacity);
        return inv;
    }
}