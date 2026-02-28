package com.yourname.booking.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_payments_booking", columnNames = {"booking_id"})
        }
)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "provider_ref", length = 120)
    private String providerRef;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = OffsetDateTime.now();
    }

    // ===== getters/setters =====
    public Long getId() { return id; }
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public String getProviderRef() { return providerRef; }
    public void setProviderRef(String providerRef) { this.providerRef = providerRef; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}