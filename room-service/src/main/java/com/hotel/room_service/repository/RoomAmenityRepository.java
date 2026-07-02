package com.hotel.room_service.repository;

import com.hotel.room_service.model.RoomAmenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomAmenityRepository extends JpaRepository<RoomAmenity, Integer> {
    @Query("SELECT ra FROM RoomAmenity ra WHERE ra.room.id IN :roomIds")
    List<RoomAmenity> findByRoomIdIn(@Param("roomIds") List<Integer> roomIds);
    List<RoomAmenity> findByRoom_Id(int roomId);
}