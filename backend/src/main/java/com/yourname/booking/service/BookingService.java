package com.yourname.booking.service;

import com.yourname.booking.dto.*;
import com.yourname.booking.dto.BookingResponse;
import com.yourname.booking.entity.*;
import com.yourname.booking.exception.BadRequestException;
import com.yourname.booking.exception.ConflictException;
import com.yourname.booking.exception.NotFoundException;
import com.yourname.booking.repository.BookingRepository;
import com.yourname.booking.repository.InventoryRepository;
import com.yourname.booking.repository.PaymentRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class BookingService {

    private static final int HOLD_MINUTES = 10;

    private final BookingRepository bookingRepository;
    private final InventoryRepository inventoryRepository;
    private final PaymentRepository paymentRepository;

    public BookingService(BookingRepository bookingRepository, InventoryRepository inventoryRepository, PaymentRepository paymentRepository) {
        this.bookingRepository = bookingRepository;
        this.inventoryRepository = inventoryRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> list(BookingStatus status, Pageable pageable) {
        Page<Booking> page = (status == null)
                ? bookingRepository.findAll(pageable)
                : bookingRepository.findByStatusOrderByCreatedAtDesc(status, pageable);

        return page.map(this::toResponse);
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
        booking.setExpiresAt(OffsetDateTime.now().plusSeconds(15));
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

    @Transactional
    public PayBookingResponse pay(UUID bookingId, String paymentIdempotencyKey, String providerRef) {
        if (paymentIdempotencyKey == null || paymentIdempotencyKey.isBlank()) {
            throw new BadRequestException("Missing idempotency-Key header");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + bookingId));

        // if already paid/confirmed, return existing payment
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            Payment payment = paymentRepository.findByBookingId(bookingId)
                    .orElseThrow(() -> new ConflictException("Booking confirmed but payment record missing"));
            return new PayBookingResponse(toResponse(booking), toPaymentResponse(payment));
        }

        // Must be pending
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new ConflictException("Booking is not payable. status=" + booking.getStatus());
        }

        // Expiry check
        if (booking.getExpiresAt() != null && booking.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new ConflictException("Booking expired");
        }

        // Payment idempotency by header key
        // If providerRef already exist, treat as idempotent retry
        var existingByProvider = paymentRepository.findByProviderRef(providerRef);
        if (existingByProvider.isPresent()) {
            Payment payment = existingByProvider.get();

            // ensure it belongs to same booking
            if (payment.getBooking().getId().equals(bookingId)) {
                throw new ConflictException("providerRef already used for another booking");
            }

            // ensure booking confirmed
            booking.setStatus(BookingStatus.CONFIRMED);
            return new PayBookingResponse(toResponse(booking), toPaymentResponse(payment));
        }

        // Create payment record
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setProviderRef(providerRef);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(OffsetDateTime.now());
        payment.setAmount(java.math.BigDecimal.ZERO);

        paymentRepository.save(payment);

        booking.setStatus(BookingStatus.CONFIRMED);

        return new PayBookingResponse(toResponse(booking), toPaymentResponse(payment));
    }

    private PaymentResponse toPaymentResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getProviderRef(),
                payment.getStatus(),
                payment.getPaidAt()
        );
    }
}

