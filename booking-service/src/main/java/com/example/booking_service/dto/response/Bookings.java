package com.example.booking_service.dto.response;

import lombok.Builder;

import java.sql.Date;

@Builder
public class Bookings {
    public int id;
    public int hotelId;
    public String hotelName;
    public Date checkInDate;
    public Date checkOutDate;
    public int guests;
    public String status;
    public String address;
    public String imageUrl;
    public double fee;
    public boolean isPaid;
}

