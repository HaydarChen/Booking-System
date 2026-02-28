package com.yourname.booking.repository;

import com.yourname.booking.entity.Booking;
import com.yourname.booking.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Optional<Booking> findByIdempotencyKey(String idempotencyKey);

    List<Booking> findTop100ByStatusAndExpiresAtBeforeOrderByExpiresAtAsc(
            BookingStatus status,
            OffsetDateTime now
    );
}