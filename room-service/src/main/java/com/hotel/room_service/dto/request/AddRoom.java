package com.hotel.room_service.dto.request;

import java.util.List;

public class AddRoom {
    public int hotelId;
    public String roomNumber;
    public String roomType;
    public int price;
    public int capacity;
    public String description;

    public List<String> amenities;
}
