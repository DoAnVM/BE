package com.hotel.room_service.repository;

import com.hotel.room_service.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface RoomRepository extends JpaRepository<Room, Integer> {
    List<Room> findByHotelid(int hotelId);

    Integer countByHotelidAndRoomTypeAndAvailable(int hotelId, String roomtype, Boolean isAvailable);

    Integer countByHotelidAndRoomType(int hotelId, String roomtype);

    List<Room> findByHotelidOrderByRoomType(int hotelId);

    List<Room> findByHotelidAndActive(int hotelid, boolean active);

    Optional<Room> findByHotelidAndRoomNumber(int hotelId, String roomNumber);
}

