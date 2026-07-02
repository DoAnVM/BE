package com.example.booking_service.kafka;

import com.example.booking_service.dto.request.BookingRequestEvent;
import com.example.booking_service.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Kafka Consumer xử lý yêu cầu đặt phòng tuần tự.
 *
 * Cơ chế chống đặt trùng:
 * - Topic "booking-request-topic" có nhiều partition.
 * - Mỗi request được gửi với key = "{hotelId}:{roomType}".
 * - Kafka đảm bảo tất cả message có cùng key đi vào cùng 1 partition.
 * - Consumer với concurrency=1 xử lý từng message MỘT CÁI MỘT trong cùng partition.
 * - Thêm DB-level SERIALIZABLE isolation → 100% không race condition.
 */
@Component
@RequiredArgsConstructor
public class BookingKafkaConsumer {

    private final BookingService bookingService;

    @KafkaListener(
            topics = "booking-request-topic",
            groupId = "booking-group",
            containerFactory = "bookingKafkaListenerContainerFactory"
    )
    public void handleBookingRequest(
            ConsumerRecord<String, BookingRequestEvent> record,
            Acknowledgment acknowledgment
    ) {
        BookingRequestEvent event = record.value();
        System.out.println("[Kafka Consumer] Nhận request: " + event.getRequestId()
                + " | key=" + record.key()
                + " | partition=" + record.partition());

        try {
            // Xử lý booking với SERIALIZABLE isolation (trong BookingService)
            bookingService.processBookingEvent(event);
            System.out.println("[Kafka Consumer] Xử lý xong requestId=" + event.getRequestId());
        } catch (Exception e) {
            System.err.println("[Kafka Consumer] Lỗi khi xử lý requestId=" + event.getRequestId()
                    + ": " + e.getMessage());
        } finally {
            // Commit offset sau khi xử lý xong (dù thành công hay thất bại)
            // để không xử lý lại cùng một request
            acknowledgment.acknowledge();
        }
    }
}
