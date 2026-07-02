package com.example.booking_service.dto.request;

import com.example.booking_service.model.BookingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.util.List;

/**
 * DTO được serialize vào Kafka topic "booking-request-topic".
 * Mỗi object này đại diện cho 1 yêu cầu đặt phòng cần xử lý.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequestEvent {
    /** ID duy nhất của request, dùng để FE polling kết quả */
    private String requestId;
    private int customerId;
    private int hotelId;
    private Date checkInDate;
    private Date checkOutDate;
    private int guests;
    private BookingType bookingType;
    private List<AddBookingItem> bookingItems;
}
