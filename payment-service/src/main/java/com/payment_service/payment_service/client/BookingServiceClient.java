package com.payment_service.payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "booking-service", url = "${BOOKING_SERVICE_URL:}")
public interface BookingServiceClient {
    
    @PutMapping("/api/booking-service/bookings/status")
    void updateBookingStatus(
        @RequestParam("bookingId") Long bookingId, 
        @RequestParam("status") String status
    );
}
