package com.hotel.hotel_service.controller;

import com.hotel.hotel_service.dto.request.SearchHotel;
import com.hotel.hotel_service.dto.request.UpdateHotelProfileRequest;
import com.hotel.hotel_service.dto.response.Hotels;
import com.hotel.hotel_service.dto.response.SearchHotelResult;
import com.hotel.hotel_service.service.HotelService;

import feign.Param;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @GetMapping("/hotels")
    public ResponseEntity<Map<String, Object>> getFamousHotels() {
        Map<String, Object> response = new HashMap<>();
        List<Hotels> hotelsList = hotelService.getFamousHotels();
        if (hotelsList != null && !hotelsList.isEmpty()) {
            response.put("hotels", hotelsList);
        } else {
            response.put("status", "Danh sách khách sạn trống");
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("allHotel")
    public ResponseEntity<Map<String, Object>> getHotels() {
        Map<String, Object> response = new HashMap<>();
        List<Hotels> hotelsList = hotelService.getAllHotels();
        if (hotelsList != null && !hotelsList.isEmpty()) {
            response.put("hotels", hotelsList);
        } else {
            response.put("status", "Danh sách khách sạn trống");
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/hotel-detail")
    public ResponseEntity<Map<String, Object>> getHotelDetail(@RequestParam("hotelId") int hotelId) {
        Map<String, Object> response = new HashMap<>();
        try {
            var hotelDetail = hotelService.getHotelDetail(hotelId);
            if (hotelDetail != null) {
                response.put("hotelDetail", hotelDetail);
            } else {
                response.put("message", "Không tìm thấy chi tiết khách sạn");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Lỗi khi lấy chi tiết khách sạn");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/hotel-profile")
    public ResponseEntity<Map<String, Object>> getHotelProfile(@RequestParam("hotelId") int hotelId) {
        Map<String, Object> response = new HashMap<>();
        try {
            var hotelProfile = hotelService.getHotelProfile(hotelId);
            if (hotelProfile != null) {
                response.put("hotelProfile", hotelProfile);
            } else {
                response.put("message", "Không tìm thấy chi tiết khách sạn");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Lỗi khi lấy chi tiết khách sạn");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/hotel-profile")
    public ResponseEntity<Map<String, Object>> updateHotelProfile(@RequestParam("hotelId") int hotelId,
            @RequestBody UpdateHotelProfileRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            hotelService.updateHotelProfile(hotelId, request);
            response.put("message", "Cập nhật hồ sơ khách sạn thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Lỗi khi cập nhật hồ sơ khách sạn: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/hotels/search")
    public ResponseEntity<Map<String, Object>> searchHotels(@RequestBody SearchHotel searchHotel) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<SearchHotelResult> results = hotelService.searchHotels(searchHotel);
            if (results != null && !results.isEmpty()) {
                response.put("hotels", results);
                response.put("message", "Tìm thấy " + results.size() + " khách sạn phù hợp");
                response.put("total", results.size());
            } else {
                response.put("hotels", new java.util.ArrayList<>());
                response.put("message", "Không tìm thấy khách sạn phù hợp");
                response.put("total", 0);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Lỗi khi tìm kiếm khách sạn: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // Endpoint service call

    @GetMapping("/hotel_detail")
    public ResponseEntity<Map<String, Object>> getHotelDetails(@RequestParam("hotelId") int hotelId) {
        Map<String, Object> response = new HashMap<>();
        try {
            var hotelDetail = hotelService.getHotelDetailForBooking(hotelId);
            if (hotelDetail != null) {
                response.put("hotelName", hotelDetail.hotelName);
                response.put("street", hotelDetail.street);
                response.put("district", hotelDetail.district);
                response.put("city", hotelDetail.city);
                response.put("country", hotelDetail.country);
                response.put("imageUrl", hotelDetail.imageUrl);
            } else {
                response.put("message", "Không tìm thấy chi tiết khách sạn");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Lỗi khi lấy chi tiết khách sạn");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/hotels/{id}/exists")
    public ResponseEntity<Map<String, Object>> checkHotelExists(@PathVariable int id) {
        Map<String, Object> response = new HashMap<>();
        boolean exists = hotelService.existsById(id);
        if (exists) {
            response.put("exists", true);
        } else {
            response.put("exists", false);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/hotelId")
    public ResponseEntity<Map<String, Object>> checkHotelId(@RequestParam("userId") int userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            int hotelId = hotelService.getHotelId(userId);
            response.put("hotelId", hotelId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Lỗi khi lấy hotelId từ userId");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/ownerId")
    public ResponseEntity<Map<String, Object>> getOwnerId(@RequestParam("hotelId") int hotelId) {
        Map<String, Object> response = new HashMap<>();
        try {
            int ownerId = hotelService.getOwnerIdByHotelId(hotelId);
            response.put("ownerId", ownerId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Lỗi khi lấy ownerId từ hotelId");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/login-hotelId")
    public ResponseEntity<Map<String, Object>> getHotelId(@RequestParam("userId") int userId,
            @RequestParam("role") Integer role) {
        Map<String, Object> response = new HashMap<>();
        try {
            int hotelId = hotelService.getHotelId(userId, role);
            response.put("hotelId", hotelId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Lỗi khi lấy hotelId từ userId");
            return ResponseEntity.internalServerError().body(response);
        }
    }

}
