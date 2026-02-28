package com.yourname.booking.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "bookings",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_bookings_idempotency_key", columnNames = {"idempotency_key"})
        },
        indexes = {
                @Index(name = "idx_bookings_inventory_id", columnList = "inventory_id"),
                @Index(name = "idx_bookings_status_expires_at", columnList = "status, expires_at")
        }
)
public class Booking {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id", nullable = false, length = 80)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BookingStatus status;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(name = "idempotency_key", nullable = false, length = 80)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        var now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.id == null) this.id = UUID.randomUUID();
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public boolean isPending() {
        return this.status == BookingStatus.PENDING_PAYMENT;
    }

    public boolean isExpired(OffsetDateTime now) {
        return this.status == BookingStatus.PENDING_PAYMENT
                && this.expiresAt != null
                && now.isAfter(this.expiresAt);
    }

    // ===== getters/setters =====
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Inventory getInventory() { return inventory; }
    public void setInventory(Inventory inventory) { this.inventory = inventory; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}