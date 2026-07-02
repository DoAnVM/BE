package com.example.booking_service.dto.response;

import lombok.Builder;

import java.sql.Date;

@Builder
public class Books {
    public int id;
    public String customerName;
    public int guestCount;
    public String roomType;
    public Date checkInDate;
    public Date checkOutDate;
    public String status;
    public String phone;
    public int totalRooms;
    public float fee;
}
