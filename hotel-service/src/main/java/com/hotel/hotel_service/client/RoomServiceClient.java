package com.hotel.hotel_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "room-service", url = "${ROOM_SERVICE_URL:}")
public interface RoomServiceClient {

    @GetMapping("/rooms/available-count")
    Map<String, Object> getAvailableRoomCount(
            @RequestParam("hotelId") int hotelId,
            @RequestParam("checkInDate") String checkInDate,
            @RequestParam("checkOutDate") String checkOutDate,
            @RequestParam("guests") int guests,
            @RequestParam("rooms") int rooms
    );

    @GetMapping("/rooms/min-price")
    Map<String, Object> getMinRoomPrice(@RequestParam("hotelId") int hotelId);
}


