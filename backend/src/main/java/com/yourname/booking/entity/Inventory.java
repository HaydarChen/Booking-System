package com.yourname.booking.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "inventories",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_inventories_type_item_code", columnNames = {"type", "item_code"})
        },
        indexes = {
                @Index(name = "idx_inventories_type", columnList = "type")
        }
)
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InventoryType type;

    @Column(name = "item_code", nullable = false, length = 120)
    private String itemCode;

    @Column(name = "total_capacity", nullable = false)
    private int totalCapacity;

    @Column(nullable = false)
    private int available;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        var now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.version == null) this.version = 0L;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    // ===== business helpers =====
    public boolean canReserve(int qty) {
        return qty > 0 && this.available >= qty;
    }

    public void reserve(int qty) {
        if (!canReserve(qty)) throw new IllegalStateException("Insufficient inventory");
        this.available -= qty;
    }

    public void release(int qty) {
        if (qty <= 0) return;
        this.available = Math.min(this.totalCapacity, this.available + qty);
    }

    // ===== getters/setters (generate via IDE) =====
    public Long getId() { return id; }
    public InventoryType getType() { return type; }
    public void setType(InventoryType type) { this.type = type; }
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public int getTotalCapacity() { return totalCapacity; }
    public void setTotalCapacity(int totalCapacity) { this.totalCapacity = totalCapacity; }
    public int getAvailable() { return available; }
    public void setAvailable(int available) { this.available = available; }
    public Long getVersion() { return version; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}