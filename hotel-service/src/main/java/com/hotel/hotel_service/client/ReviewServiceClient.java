package com.hotel.hotel_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "review-service", url = "${REVIEW_SERVICE_URL:}")
public interface ReviewServiceClient {

    @GetMapping("/averageStar")
    Map<String, Object> getAverageStarByHotelId(@RequestParam int hotelId);

    @GetMapping("/countComment")
    Map<String, Object> getCountCommentByHotelId(@RequestParam int hotelId);
}
