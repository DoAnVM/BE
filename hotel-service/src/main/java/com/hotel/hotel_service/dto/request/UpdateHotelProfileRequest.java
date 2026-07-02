package com.hotel.hotel_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateHotelProfileRequest {
    public String hotelName;
    public String street;
    public String district;
    public String city;
    public String country;
    public String description;
    public List<String> amenities;
    public List<String> imageUrls;
}
