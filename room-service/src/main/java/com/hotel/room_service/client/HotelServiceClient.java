package com.hotel.room_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "hotel-service", url = "${HOTEL_SERVICE_URL:}")
public interface HotelServiceClient {
    @GetMapping("/hotelId")
    Map<String, Object> checkHotelId(@RequestParam("userId") int userId);
}
