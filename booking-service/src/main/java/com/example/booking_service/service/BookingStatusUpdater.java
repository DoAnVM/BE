package com.example.booking_service.service;

import com.example.booking_service.model.BookingRequest;
import com.example.booking_service.repository.BookingRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service chuyên cập nhật trạng thái BookingRequest.
 *
 * Tại sao cần class riêng?
 * - Spring AOP chỉ intercept @Transactional khi gọi qua proxy (bean khác).
 * - Nếu BookingService tự gọi method của mình, @Transactional với
 *   Propagation.REQUIRES_NEW sẽ KHÔNG tạo transaction mới.
 * - Tách ra class riêng → Spring proxy hoạt động đúng.
 *
 * REQUIRES_NEW: Luôn mở transaction MỚI, commit NGAY LẬP TỨC và độc lập
 * với transaction của caller. Đảm bảo status được ghi dù transaction
 * booking chính có rollback hay timeout.
 */
@Service
@RequiredArgsConstructor
public class BookingStatusUpdater {

    private final BookingRequestRepository bookingRequestRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSuccess(String requestId, Integer bookingId) {
        bookingRequestRepository.findById(requestId).ifPresent(req -> {
            req.setStatus("SUCCESS");
            req.setBookingId(bookingId);
            req.setProcessedAt(LocalDateTime.now());
            bookingRequestRepository.save(req);
            System.out.println("[StatusUpdater] requestId=" + requestId + " → SUCCESS, bookingId=" + bookingId);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(String requestId, String errorMessage) {
        bookingRequestRepository.findById(requestId).ifPresent(req -> {
            req.setStatus("FAILED");
            req.setErrorMessage(errorMessage);
            req.setProcessedAt(LocalDateTime.now());
            bookingRequestRepository.save(req);
            System.out.println("[StatusUpdater] requestId=" + requestId + " → FAILED: " + errorMessage);
        });
    }
}
