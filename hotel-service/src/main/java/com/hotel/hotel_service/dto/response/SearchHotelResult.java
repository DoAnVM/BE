package com.hotel.hotel_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchHotelResult {
    public int hotelId;
    public String hotelName;
    public String street;
    public String district;
    public String city;
    public String country;
    public double rating;
    public String imageUrl;
    public int availableRooms;
    public int totalRooms;
}

