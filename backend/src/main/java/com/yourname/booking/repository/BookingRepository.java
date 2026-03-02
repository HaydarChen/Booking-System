package com.yourname.booking.repository;

import com.yourname.booking.entity.Booking;
import com.yourname.booking.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Optional<Booking> findByIdempotencyKey(String idempotencyKey);

    @Query("""
        select b from Booking b
        join fetch b.inventory i
        where b.status = :status
          and b.expiresAt < :now
        order by b.expiresAt asc
    """)
    List<Booking> findExpiredPendingWithInventory(
            @Param("status") BookingStatus status,
            @Param("now") OffsetDateTime now,
            Pageable pageable
    );
}