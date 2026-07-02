package com.payment_service.payment_service.controller;

import com.payment_service.payment_service.dto.CreatePaymentRequest;
import com.payment_service.payment_service.dto.PaymentSessionResponse;
import com.payment_service.payment_service.model.Payment;
import com.payment_service.payment_service.repository.PaymentRepository;
import com.payment_service.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;
import vn.payos.model.webhooks.Webhook;

import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping({"/api/payment-service", ""})
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PayOS payOS;
    private final PaymentRepository paymentRepository;

    /**
     * Tạo link thanh toán PayOS
     * POST /api/payment-service/payos/create-payment-link
     */
    @PostMapping("/payos/create-payment-link")
    public ResponseEntity<?> createPaymentLink(@RequestBody CreatePaymentRequest request) {
        try {
            log.info("Received payment request for bookingId={}, amount={}", request.getBookingId(),
                    request.getAmount());
            PaymentSessionResponse response = paymentService.createPaymentLink(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating payment link: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Lấy trạng thái thanh toán theo bookingId (dùng nội bộ bởi booking-service)
     * GET /api/payment-service/payos/status/booking/{bookingId}
     * Trả về: { "isPaid": true/false, "status": "PAID" / "PENDING" / "NOT_FOUND" }
     */
    @GetMapping("/payos/status/booking/{bookingId}")
    public ResponseEntity<?> getPaymentStatusByBookingId(@PathVariable Long bookingId) {
        Optional<Payment> paymentOpt = paymentRepository.findByBookingId(bookingId);
        if (paymentOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("isPaid", false, "status", "NOT_FOUND"));
        }
        Payment payment = paymentOpt.get();
        boolean isPaid = "PAID".equalsIgnoreCase(payment.getStatus());
        return ResponseEntity.ok(Map.of(
                "isPaid", isPaid,
                "status", payment.getStatus() != null ? payment.getStatus() : "UNKNOWN"
        ));
    }

    /**
     * Lấy thông tin thanh toán theo orderCode
     * GET /api/payment-service/payos/payment/{orderCode}
     */
    @GetMapping("/payos/payment/{orderCode}")
    public ResponseEntity<?> getPaymentByOrderCode(@PathVariable Long orderCode) {
        try {
            PaymentSessionResponse response = paymentService.getPaymentByOrderCode(orderCode);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting payment info: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Hủy thanh toán
     * POST /api/payment-service/payos/cancel/{orderCode}
     */
    @PostMapping("/payos/cancel/{orderCode}")
    public ResponseEntity<?> cancelPayment(@PathVariable Long orderCode) {
        try {
            paymentService.cancelPayment(orderCode);
            return ResponseEntity.ok(Map.of("message", "Đã hủy thanh toán thành công"));
        } catch (Exception e) {
            log.error("Error cancelling payment: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Webhook từ PayOS - nhận thông báo khi thanh toán thành công/thất bại
     * POST /api/payment-service/payos/webhook
     */
    @PostMapping("/payos/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody Webhook webhook) {
        try {
            log.info("Received PayOS webhook");
            var data = payOS.webhooks().verify(webhook);
            log.info("PayOS webhook verified - orderCode={}, status={}", data.getOrderCode(), data.getDesc());

            // Nếu thanh toán thành công
            if ("00".equals(data.getCode()) || "PAID".equalsIgnoreCase(data.getDesc())) {
                paymentService.handlePaymentSuccess(data.getOrderCode());
            } else {
                log.warn("PayOS webhook - payment not successful: code={}, desc={}", data.getCode(), data.getDesc());
            }

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Webhook verification failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Invalid webhook");
        }
    }

    /**
     * Return URL handler - xác nhận thanh toán thành công sau khi redirect từ PayOS
     * GET /api/payment-service/payos/success
     */
    @GetMapping("/payos/success")
    public ResponseEntity<?> handlePaymentSuccess(
            @RequestParam(value = "orderCode", required = false) Long orderCode,
            @RequestParam(value = "status", required = false) String status) {
        try {
            if (orderCode != null && "PAID".equalsIgnoreCase(status)) {
                paymentService.handlePaymentSuccess(orderCode);
                log.info("Payment confirmed via return URL - orderCode={}", orderCode);
            }
            PaymentSessionResponse payment = paymentService.getPaymentByOrderCode(orderCode);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            log.error("Error handling payment return: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
