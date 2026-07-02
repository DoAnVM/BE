package com.hotel.room_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "booking-service", url = "${BOOKING_SERVICE_URL:}")
public interface BookingServiceClient {

    @GetMapping("/bookings/booked-rooms")
    Map<String, Object> getBookedRooms(
            @RequestParam("hotelId") int hotelId,
            @RequestParam("checkInDate") String checkInDate,
            @RequestParam("checkOutDate") String checkOutDate
    );
}

