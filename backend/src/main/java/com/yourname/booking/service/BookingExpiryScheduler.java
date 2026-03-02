package com.yourname.booking.service;

import com.yourname.booking.entity.Booking;
import com.yourname.booking.entity.BookingStatus;
import com.yourname.booking.repository.BookingRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.util.List;

@Component
public class BookingExpiryScheduler {

    private final BookingRepository bookingRepository;

    public BookingExpiryScheduler(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Scheduled(fixedDelayString = "${booking.expiry.fixed-delay-ms:10000}")
    @Transactional
    public void expirePendingBookings() {
        OffsetDateTime now = OffsetDateTime.now();
        int batchSize = 100;

        List<Booking> expired = bookingRepository.findExpiredPendingWithInventory(
                BookingStatus.PENDING_PAYMENT,
                now,
                PageRequest.of(0, batchSize)
        );

        for (Booking b : expired) {
            if (b.getStatus() != BookingStatus.PENDING_PAYMENT) continue;

            b.setStatus(BookingStatus.EXPIRED);

            b.getInventory().release(b.getQuantity());
        }
    }
}
