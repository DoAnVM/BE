package com.hotel.hotel_service.Iservice;

import com.hotel.hotel_service.dto.request.SearchHotel;
import com.hotel.hotel_service.dto.request.UpdateHotelProfileRequest;
import com.hotel.hotel_service.dto.response.HotelDetail;
import com.hotel.hotel_service.dto.response.HotelProfile;
import com.hotel.hotel_service.dto.response.Hotels;
import com.hotel.hotel_service.dto.response.SearchHotelResult;

import java.util.List;

public interface IHotelService {
    List<Hotels> getFamousHotels();
    List<Hotels> getAllHotels();
    HotelDetail getHotelDetail(int hotelId);
    HotelProfile getHotelProfile(int hotelId);
    void updateHotelProfile(int hotelId, UpdateHotelProfileRequest request);

    List<SearchHotelResult> searchHotels(SearchHotel searchHotel);
    // Booking service call
    boolean existsById(int hotelId);
    HotelDetail getHotelDetailForBooking(int hotelId);
    // Room service call
    int getHotelId(int userId);

    // Review service call
    int getOwnerIdByHotelId(int hotelId);

    //Hotel call
    int getHotelId(int ownerId, Integer role);
}
