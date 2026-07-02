package com.example.booking_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Lưu trạng thái xử lý của mỗi yêu cầu đặt phòng qua Kafka.
 * Trạng thái: PENDING → SUCCESS hoặc FAILED
 */
@Entity
@Table(name = "booking_requests")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequest {

    @Id
    @Column(name = "request_id", length = 64, nullable = false)
    private String requestId;

    @Column(name = "customer_id", nullable = false)
    private int customerId;

    @Column(name = "hotel_id", nullable = false)
    private int hotelId;

    /** PENDING | SUCCESS | FAILED */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /** bookingId trả về sau khi xử lý thành công (null nếu PENDING/FAILED) */
    @Column(name = "booking_id")
    private Integer bookingId;

    /** Thông báo lỗi (null nếu PENDING/SUCCESS) */
    @Column(name = "error_message", length = 512)
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}
