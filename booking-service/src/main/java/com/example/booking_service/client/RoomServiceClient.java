package com.example.booking_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "room-service", url = "${ROOM_SERVICE_URL:}")
public interface RoomServiceClient {

    @GetMapping("/rooms")
    Map<String, Object> getRoomsByHotelId(@RequestParam("hotelId") int hotelId);

}
