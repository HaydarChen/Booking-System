package com.yourname.booking.service;

import com.yourname.booking.dto.BookingResponse;
import com.yourname.booking.dto.CreateBookingRequest;
import com.yourname.booking.entity.Booking;
import com.yourname.booking.entity.BookingStatus;
import com.yourname.booking.entity.Inventory;
import com.yourname.booking.exception.BadRequestException;
import com.yourname.booking.exception.ConflictException;
import com.yourname.booking.exception.NotFoundException;
import com.yourname.booking.repository.BookingRepository;
import com.yourname.booking.repository.InventoryRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class BookingService {

    private static final int HOLD_MINUTES = 10;

    private final BookingRepository bookingRepository;
    private final InventoryRepository inventoryRepository;

    public BookingService(BookingRepository bookingRepository, InventoryRepository inventoryRepository) {
        this.bookingRepository = bookingRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BadRequestException("Missing idempotency-Key header");
        }

        // Idempotency fast-path
        var existing = bookingRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }

        // Load inventory (managed entity)
        Inventory inventory = inventoryRepository.findById(request.inventoryId())
                .orElseThrow(() ->
                        new NotFoundException("Inventory not found"));

        // Check stock
        if (!inventory.canReserve(request.quantity())) {
            throw new ConflictException("Insufficient inventory");
        }

        // Reserve
        inventory.reserve(request.quantity());

        // Create booking
        Booking booking = new Booking();
        booking.setUserId("demo-user"); // Use auth
        booking.setInventory(inventory);
        booking.setQuantity(request.quantity());
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setExpiresAt(OffsetDateTime.now().plusMinutes(HOLD_MINUTES));
        booking.setIdempotencyKey(idempotencyKey);

        try {
            bookingRepository.save(booking);
        } catch (DataIntegrityViolationException e) {
            // Race condition
            return bookingRepository.findByIdempotencyKey(idempotencyKey)
                    .map(this::toResponse)
                    .orElseThrow(() -> e);
        }

        return toResponse(booking);
    }

    @Transactional(readOnly = true)
    public BookingResponse get(UUID id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + id));
        return toResponse(booking);
    }
    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getInventory().getId(),
                booking.getQuantity(),
                booking.getStatus(),
                booking.getExpiresAt()
        );
    }
}
