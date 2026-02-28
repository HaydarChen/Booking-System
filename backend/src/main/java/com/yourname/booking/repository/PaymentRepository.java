package com.yourname.booking.repository;

import com.yourname.booking.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBookingId(UUID bookingId);
}