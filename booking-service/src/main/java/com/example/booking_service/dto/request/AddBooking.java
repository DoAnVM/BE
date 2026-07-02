package com.example.booking_service.dto.request;


import com.example.booking_service.model.BookingType;

import java.sql.Date;
import java.util.List;

public class AddBooking {
    public int hotelId;
    public Date checkInDate;
    public Date checkOutDate;
    public int guests;
    public BookingType bookingType; // hoặc BookingType enum
    public List<AddBookingItem> bookingItems; // danh sách các loại phòng đặt
}

