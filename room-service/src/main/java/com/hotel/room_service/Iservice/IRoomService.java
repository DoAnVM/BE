package com.hotel.room_service.Iservice;

import com.hotel.room_service.dto.request.AddRoom;
import com.hotel.room_service.dto.request.UpdateActive;
import com.hotel.room_service.dto.request.UpdateAvailability;
import com.hotel.room_service.dto.request.UpdateRoom;
import com.hotel.room_service.dto.response.RoomByHotel;
import com.hotel.room_service.dto.response.Rooms;

import java.util.List;
import java.util.Map;

public interface IRoomService {
    List<RoomByHotel> findRoomByHotelId(int userId);
    Map<String, Integer> getAvailableRoomCount(int hotelId, String checkInDate, String checkOutDate, int guests, int rooms);
    Boolean updateRoom(UpdateRoom updateRoom);
    Boolean addRoom(AddRoom addRoom);
    Boolean activeRoom(UpdateActive updateActive);
    Boolean updateAvailability(UpdateAvailability updateAvailability);
    List<Rooms> getAllRooms(int userId);
    Rooms getRoomDetail(int roomId);
}
