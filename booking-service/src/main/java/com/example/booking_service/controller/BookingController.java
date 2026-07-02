package com.example.booking_service.controller;

import com.example.booking_service.dto.request.AddBooking;
import com.example.booking_service.dto.request.CheckInCheckOut;
import com.example.booking_service.dto.request.Update;
import com.example.booking_service.model.BookingRequest;
import com.example.booking_service.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RequestMapping
@RestController
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * POST /booking
     * Gửi yêu cầu đặt phòng vào Kafka.
     * Trả về requestId để FE dùng polling kiểm tra kết quả.
     *
     * Response: { "requestId": "uuid", "status": "PENDING", "message": "..." }
     */
    @PostMapping("/booking")
    public ResponseEntity<Map<String, Object>> booking(
            @RequestHeader("X-Auth-UserId") String userIdStr,
            @RequestBody AddBooking booking) {
        Map<String, Object> response = new HashMap<>();
        try {
            int customerId = Integer.parseInt(userIdStr);

            // Gửi vào Kafka và lưu trạng thái PENDING
            String requestId = bookingService.submitBookingRequest(customerId, booking);

            response.put("requestId", requestId);
            response.put("status", "PENDING");
            response.put("message", "Yêu cầu đặt phòng đang được xử lý. Vui lòng chờ...");
            return ResponseEntity.accepted().body(response);

        } catch (NumberFormatException e) {
            response.put("message", "Invalid X-Auth-UserId");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("message", "Đã xảy ra lỗi khi gửi yêu cầu đặt phòng.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * GET /booking/status/{requestId}
     * FE gọi endpoint này để polling kết quả xử lý booking.
     *
     * Response khi PENDING:  { "status": "PENDING" }
     * Response khi SUCCESS:  { "status": "SUCCESS", "bookingId": 123 }
     * Response khi FAILED:   { "status": "FAILED",  "message": "Lý do thất bại" }
     */
    @GetMapping("/booking/status/{requestId}")
    public ResponseEntity<Map<String, Object>> getBookingStatus(@PathVariable String requestId) {
        Map<String, Object> response = new HashMap<>();
        try {
            BookingRequest bookingRequest = bookingService.getBookingRequestStatus(requestId);
            if (bookingRequest == null) {
                response.put("message", "Không tìm thấy yêu cầu đặt phòng.");
                return ResponseEntity.status(404).body(response);
            }

            response.put("status", bookingRequest.getStatus());

            switch (bookingRequest.getStatus()) {
                case "SUCCESS" -> {
                    response.put("bookingId", bookingRequest.getBookingId());
                    response.put("message", "Đặt phòng thành công!");
                }
                case "FAILED" -> {
                    response.put("message", bookingRequest.getErrorMessage());
                }
                default -> {
                    response.put("message", "Yêu cầu đang được xử lý...");
                }
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Lỗi khi kiểm tra trạng thái đặt phòng.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/bookings")
    public ResponseEntity<Map<String, Object>> getBookingsByCustomerId(
            @RequestHeader("X-Auth-UserId") String userIdStr) {
        Map<String, Object> response = new HashMap<>();
        try {
            int customerId = Integer.parseInt(userIdStr);
            var bookings = bookingService.getBookingsByCustomerId(customerId);
            if (bookings != null && !bookings.isEmpty()) {
                response.put("message", "Get bookings successfully");
                response.put("bookings", bookings);
            } else {
                response.put("message", "No bookings found");
            }
            return ResponseEntity.ok(response);
        } catch (NumberFormatException e) {
            response.put("message", "Invalid X-Auth-UserId");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("message", "Error occurred while retrieving bookings");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/bookings/booked-rooms")
    public ResponseEntity<Map<String, Object>> getBookedRooms(
            @RequestParam("hotelId") int hotelId,
            @RequestParam("checkInDate") String checkInDate,
            @RequestParam("checkOutDate") String checkOutDate) {
        Map<String, Object> response = new HashMap<>();
        try {
            int bookedRooms = bookingService.getBookedRooms(hotelId, checkInDate, checkOutDate);
            response.put("bookedRooms", bookedRooms);
            response.put("hotelId", hotelId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("bookedRooms", 0);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/bookings/status")
    public ResponseEntity<Map<String, Object>> updateBookingStatus(@RequestBody Update update) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean updated = bookingService.updateBookingStatus(update);
            if (updated) {
                response.put("message", "Booking status updated successfully");
            } else {
                response.put("message", "Failed to update booking status");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Error occurred while updating booking status");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/owner/bookings")
    public ResponseEntity<Map<String, Object>> getBookingsByHotelOwnerId(
            @RequestParam("hotelId") int hotelId) {
        Map<String, Object> response = new HashMap<>();
        try {
            var bookings = bookingService.getBookingsByHotelId(hotelId);
            if (bookings != null && !bookings.isEmpty()) {
                response.put("message", "Get bookings successfully");
                response.put("bookings", bookings);
            } else {
                response.put("message", "No bookings found");
            }
            return ResponseEntity.ok(response);
        } catch (NumberFormatException e) {
            response.put("message", "Invalid hotelId");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("message", "Error occurred while retrieving bookings");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/checkInCheckOut")
    public ResponseEntity<Map<String, Object>> checkInCheckOut(
            @RequestBody CheckInCheckOut checkInCheckOut) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean check = bookingService.checkIntOutHotel(checkInCheckOut);
            if (check) {
                response.put("message", "Check-in/Check-out successful");
            } else {
                response.put("message", "Check-in/Check-out failed");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Error occurred during Check-in/Check-out");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
