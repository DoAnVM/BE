package com.example.booking_service.Iservice;

import com.example.booking_service.dto.request.AddBooking;
import com.example.booking_service.dto.request.CheckInCheckOut;
import com.example.booking_service.dto.request.Update;
import com.example.booking_service.dto.response.Bookings;
import com.example.booking_service.dto.response.Books;

import java.util.List;

public interface IBookingService {
    Integer addBooking(int customerId, AddBooking addBooking);
    List<Bookings> getBookingsByCustomerId(int customerId);
    int getBookedRooms(int hotelId, String checkInDate, String checkOutDate);
    boolean updateBookingStatus(Update update);
    List<Books> getBookingsByHotelId(int hotelId);
    boolean checkIntOutHotel(CheckInCheckOut checkInCheckOut);
}
