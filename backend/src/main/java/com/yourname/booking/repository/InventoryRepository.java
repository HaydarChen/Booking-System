package com.yourname.booking.repository;

import com.yourname.booking.entity.Inventory;
import com.yourname.booking.entity.InventoryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByTypeAndItemCode(InventoryType type, String itemCode);

    Page<Inventory> findByType(InventoryType type, Pageable pageable);
}