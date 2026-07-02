package com.example.booking_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "hotel-service", url = "${HOTEL_SERVICE_URL:}")
public interface HotelServiceClient {

    @GetMapping("/hotels/{id}/exists")
    Map<String, Object> checkHotelExists(@PathVariable("id") int id);

    @GetMapping("/hotel_detail")
    Map<String, Object> getHotelDetail(@RequestParam("hotelId") int hotelId);

    @GetMapping("/hotelId")
    Map<String, Object> checkHotelId(@RequestParam("userId") int userId);
}
