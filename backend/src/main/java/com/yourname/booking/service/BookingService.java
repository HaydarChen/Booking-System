package com.yourname.booking.service;

import com.yourname.booking.dto.BookingResponse;
import com.yourname.booking.dto.CreateBookingRequest;
import com.yourname.booking.entity.*;
import com.yourname.booking.exception.NotFoundException;
import com.yourname.booking.repository.BookingRepository;
import com.yourname.booking.repository.InventoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.awt.print.Book;
import java.time.OffsetDateTime;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final InventoryRepository inventoryRepository;

    public BookingService(BookingRepository bookingRepository, InventoryRepository inventoryRepository) {
        this.bookingRepository = bookingRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request, String idempotencyKey) {

        // Check idempotency
        var existing = bookingRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }

        // Load inventory
        Inventory inventory = inventoryRepository.findById(request.inventoryId())
                .orElseThrow(() ->
                        new NotFoundException("Inventory not found"));

        // Check stock
        if (!inventory.canReserve(request.quantity())) {
            throw new IllegalStateException("Insufficient inventory");
        }

        // Reserve
        inventory.reserve(request.quantity());

        // Create booking
        Booking booking = new Booking();
        booking.setUserId("demo-user"); // Use auth
        booking.setInventory(inventory);
        booking.setQuantity(request.quantity());
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setExpiresAt(OffsetDateTime.now().plusMinutes(30));
        booking.setIdempotencyKey(idempotencyKey);

        bookingRepository.save(booking);

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
