package com.payment_service.payment_service.service;

import com.payment_service.payment_service.Iservice.IPaymentService;
import com.payment_service.payment_service.client.BookingServiceClient;
import com.payment_service.payment_service.dto.CreatePaymentRequest;
import com.payment_service.payment_service.dto.PaymentSessionResponse;
import com.payment_service.payment_service.model.Payment;
import com.payment_service.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {

    private final PayOS payOS;
    private final PaymentRepository paymentRepository;
    private final BookingServiceClient bookingServiceClient;

    @Value("${payos.return-url}")
    private String returnUrl;

    @Value("${payos.cancel-url}")
    private String cancelUrl;

    @Value("${payos.client-id}")
    private String clientId;

    @Value("${payos.api-key}")
    private String apiKey;

    @Value("${payos.checksum-key}")
    private String checksumKey;

    @Override
    public PaymentSessionResponse createPaymentLink(CreatePaymentRequest request) {
        try {
            if (request.getBookingId() == null || request.getBookingId() <= 0) {
                throw new RuntimeException("Thiếu bookingId hợp lệ");
            }
            if (isPlaceholder(clientId) || isPlaceholder(apiKey) || isPlaceholder(checksumKey)) {
                throw new RuntimeException("Cấu hình PayOS API key/client id/checksum chưa hợp lệ");
            }
            if (isPlaceholder(returnUrl) || isPlaceholder(cancelUrl)) {
                throw new RuntimeException("Cấu hình URL callback PayOS chưa hợp lệ");
            }
            // Generate unique orderCode from timestamp
            long orderCode = System.currentTimeMillis();

            // PayOS requires amount in VND (integer), min 2000
            long amount = Math.max(request.getAmount(), 2000L);

            // Description max 25 chars for PayOS, only alphanumeric and spaces allowed
            String description = truncate("Thanh toan RP " + request.getBookingId(), 25);

            // Build PayOS payment link request
            String safeReturnUrl = buildCallbackUrl(returnUrl, orderCode);
            String safeCancelUrl = buildCallbackUrl(cancelUrl, orderCode);
            CreatePaymentLinkRequest paymentLinkRequest = CreatePaymentLinkRequest.builder()
                    .orderCode(orderCode)
                    .amount(amount)
                    .description(description)
                    .returnUrl(safeReturnUrl)
                    .cancelUrl(safeCancelUrl)
                    .build();

            log.info("Creating PayOS payment link for bookingId={}, orderCode={}, amount={}",
                    request.getBookingId(), orderCode, amount);

            CreatePaymentLinkResponse response = payOS.paymentRequests().create(paymentLinkRequest);

            // PayOS trả về qrCode (chuỗi EMV) ngay trong response tạo link
            String qrCode = response.getQrCode();
            log.info("PayOS payment link created: checkoutUrl={}, hasQrCode={}",
                    response.getCheckoutUrl(), qrCode != null);

            // Lưu payment vào DB, bao gồm qrCode để dùng lại sau
            Payment payment = Payment.builder()
                    .bookingId(request.getBookingId())
                    .orderCode(String.valueOf(orderCode))
                    .amount(amount)
                    .status("PENDING")
                    .provider("PAYOS")
                    .transactionId(String.valueOf(response.getPaymentLinkId()))
                    .checkoutUrl(response.getCheckoutUrl())
                    .qrCode(qrCode)
                    .bin(response.getBin())
                    .accountNumber(response.getAccountNumber())
                    .accountName(response.getAccountName())
                    .expiredAt(LocalDateTime.now().plusMinutes(15))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            paymentRepository.save(payment);

            return PaymentSessionResponse.builder()
                    .sessionUrl(response.getCheckoutUrl())
                    .sessionId(String.valueOf(orderCode))
                    .bookingId(request.getBookingId())
                    .amount(amount)
                    .status("PENDING")
                    .bin(response.getBin())
                    .accountNumber(response.getAccountNumber())
                    .accountName(response.getAccountName())
                    .description(description)
                    .qrCode(qrCode)
                    .build();

        } catch (Exception e) {
            log.error("Error creating PayOS payment link: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tạo link thanh toán: " + e.getMessage());
        }
    }

    @Override
    public PaymentSessionResponse getPaymentByOrderCode(Long orderCode) {
        Optional<Payment> paymentOpt = paymentRepository.findByOrderCode(String.valueOf(orderCode));
        if (paymentOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy thông tin thanh toán với orderCode: " + orderCode);
        }

        Payment payment = paymentOpt.get();

        // Lấy qrCode đã lưu từ DB (không cần gọi lại PayOS API)
        return PaymentSessionResponse.builder()
                .sessionId(payment.getOrderCode())
                .sessionUrl(payment.getCheckoutUrl())
                .bookingId(payment.getBookingId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .bin(payment.getBin())
                .accountNumber(payment.getAccountNumber())
                .accountName(payment.getAccountName())
                .description("BOOKING-" + payment.getBookingId())
                .qrCode(payment.getQrCode())
                .build();
    }

    @Override
    public void cancelPayment(Long orderCode) {
        try {
            Optional<Payment> paymentOpt = paymentRepository.findByOrderCode(String.valueOf(orderCode));
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                payment.setStatus("CANCELLED");
                payment.setUpdatedAt(LocalDateTime.now());
                paymentRepository.save(payment);

                // Notify booking service
                try {
                    bookingServiceClient.updateBookingStatus(payment.getBookingId(), "CANCELLED");
                } catch (Exception e) {
                    log.warn("Could not update booking status: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error cancelling payment: {}", e.getMessage(), e);
        }
    }

    /**
     * Xử lý webhook từ PayOS khi thanh toán thành công
     */
    public void handlePaymentSuccess(Long orderCode) {
        Optional<Payment> paymentOpt = paymentRepository.findByOrderCode(String.valueOf(orderCode));
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();

            if (!"PAID".equals(payment.getStatus())) {
                payment.setStatus("PAID");
                payment.setUpdatedAt(LocalDateTime.now());
                paymentRepository.save(payment);

                // Notify booking service that payment is confirmed
                try {
                    bookingServiceClient.updateBookingStatus(payment.getBookingId(), "CONFIRMED");
                    log.info("Booking {} status updated to CONFIRMED", payment.getBookingId());
                } catch (Exception e) {
                    log.warn("Could not update booking status: {}", e.getMessage());
                }
            }
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }

    private String buildCallbackUrl(String baseUrl, long orderCode) {
        String normalized = normalizeUrl(baseUrl);
        String separator = normalized.contains("?") ? "&" : "?";
        return normalized + separator + "orderCode=" + orderCode;
    }

    private String normalizeUrl(String url) {
        if (url == null) return "";
        return url.trim().replaceAll("(?<!:)/{2,}", "/");
    }

    private boolean isPlaceholder(String value) {
        if (value == null) return true;
        String lower = value.trim().toLowerCase();
        return lower.contains("your-") || lower.contains("example") || lower.isBlank();
    }
}
