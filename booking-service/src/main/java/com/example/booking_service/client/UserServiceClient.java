package com.example.booking_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "user-service", url = "${USER_SERVICE_URL:}")
public interface UserServiceClient {
    @GetMapping("/fullName")
    Map<String, Object> Customer(@RequestParam("userId") int userId);

    @GetMapping("/hotelId")
    Map<String, Object> getHotelId(@RequestParam ("ownerId") int userId);
}
