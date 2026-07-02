package com.hotel.room_service.service;

import com.hotel.room_service.Iservice.IRoomService;
import com.hotel.room_service.client.BookingServiceClient;
import com.hotel.room_service.client.HotelServiceClient;
import com.hotel.room_service.dto.request.AddRoom;
import com.hotel.room_service.dto.request.UpdateActive;
import com.hotel.room_service.dto.request.UpdateAvailability;
import com.hotel.room_service.dto.request.UpdateRoom;
import com.hotel.room_service.dto.response.RoomByHotel;
import com.hotel.room_service.dto.response.Rooms;
import com.hotel.room_service.model.Room;
import com.hotel.room_service.model.RoomAmenity;
import com.hotel.room_service.repository.RoomAmenityRepository;
import com.hotel.room_service.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService implements IRoomService {

    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private BookingServiceClient bookingServiceClient;
    @Autowired
    private RoomAmenityRepository roomAmenityRepository;
    @Autowired
    private HotelServiceClient hotelServiceClient;

    @Override
    public List<RoomByHotel> findRoomByHotelId(int hotelId) {
        List<Room> roomOptional = roomRepository.findByHotelidOrderByRoomType(hotelId);

        if (roomOptional.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, RoomByHotel> roomMap = new LinkedHashMap<>();
        Map<String, Set<String>> amenitiesByRoomType = new HashMap<>();

        List<Integer> roomIds = roomOptional.stream()
                .map(Room::getId)
                .collect(Collectors.toList());

        List<RoomAmenity> allAmenities = roomAmenityRepository.findByRoomIdIn(roomIds);

        Map<Integer, List<RoomAmenity>> amenitiesByRoomId = allAmenities.stream()
                .collect(Collectors.groupingBy(amenity -> amenity.getRoom().getId()));

        for (Room room : roomOptional) {
            String roomType = room.getRoomType();
            Set<String> amenities = amenitiesByRoomType.computeIfAbsent(roomType, k -> new HashSet<>());

            List<RoomAmenity> roomAmenities = amenitiesByRoomId.getOrDefault(room.getId(), new ArrayList<>());
            for (RoomAmenity amenity : roomAmenities) {
                amenities.add(amenity.getAmenityName());
            }
        }

        // Tạo RoomByHotel cho mỗi room type
        for (Room room : roomOptional) {
            String roomType = room.getRoomType();

            if (!roomMap.containsKey(roomType)) {
                RoomByHotel roomByHotel = new RoomByHotel();
                roomByHotel.roomType = roomType;
                roomByHotel.capacity = room.getCapacity();
                roomByHotel.description = room.getDescription();
                roomByHotel.pricePerNight = room.getPrice_per_night();

                roomByHotel.availableRooms = roomRepository.countByHotelidAndRoomTypeAndAvailable(hotelId, roomType, true);
                roomByHotel.unavailableRooms = roomRepository.countByHotelidAndRoomTypeAndAvailable(hotelId, roomType, false);
                roomByHotel.totalRooms = roomRepository.countByHotelidAndRoomType(hotelId, roomType);

                Set<String> amenities = amenitiesByRoomType.getOrDefault(roomType, new HashSet<>());
                roomByHotel.amenities = String.join(", ", amenities);

                roomMap.put(roomType, roomByHotel);
            }
        }

        return new ArrayList<>(roomMap.values());
    }


    @Override
    public Map<String, Integer> getAvailableRoomCount(int hotelId, String checkInDate, String checkOutDate, int guests, int rooms) {
        Map<String, Integer> result = new HashMap<>();

        try {
            List<Room> allRooms = roomRepository.findByHotelidAndActive(hotelId, true);
            int totalRoomsCount = 0;

            for (Room room : allRooms) {
                if (room.getCapacity() >= guests) {
                    totalRoomsCount++;
                }
            }

            Map<String, Object> bookingResponse = bookingServiceClient.getBookedRooms(
                hotelId,
                checkInDate,
                checkOutDate
            );

            Integer bookedRooms = (Integer) bookingResponse.get("bookedRooms");
            if (bookedRooms == null) {
                bookedRooms = 0;
            }

            int availableRooms = totalRoomsCount - bookedRooms;
            if (availableRooms < 0) {
                availableRooms = 0;
            }

            result.put("availableRooms", availableRooms);
            result.put("totalRooms", totalRoomsCount);
            result.put("bookedRooms", bookedRooms);

        } catch (Exception e) {
            result.put("availableRooms", 0);
            result.put("totalRooms", 0);
            result.put("bookedRooms", 0);
            System.err.println("Error getting available room count: " + e.getMessage());
        }

        return result;
    }

    @Override
    public Boolean updateRoom(UpdateRoom updateRoom) {
        // Tìm room theo ID
        Optional<Room> roomOptional = roomRepository.findById(updateRoom.roomId);
        if (roomOptional.isEmpty()) {
            throw new RuntimeException("Không tìm thấy phòng với ID: " + updateRoom.roomId);
        }

        Room room = roomOptional.get();

        // Chỉ update các trường không null
        if (updateRoom.roomType != null) {
            room.setRoomType(updateRoom.roomType);
        }

        if (updateRoom.roomPrice != null) {
            room.setPrice_per_night(updateRoom.roomPrice);
        }

        if (updateRoom.roomCapacity != null) {
            room.setCapacity(updateRoom.roomCapacity);
        }

        if (updateRoom.roomDescription != null) {
            room.setDescription(updateRoom.roomDescription);
        }

        roomRepository.save(room);

        if (updateRoom.amenities != null) {
            List<RoomAmenity> oldAmenities = roomAmenityRepository.findByRoom_Id(updateRoom.roomId);
            roomAmenityRepository.deleteAll(oldAmenities);

            List<RoomAmenity> newAmenities = new ArrayList<>();
            for (String amenityName : updateRoom.amenities) {
                RoomAmenity amenity = new RoomAmenity();
                amenity.setRoom(room);
                amenity.setAmenityName(amenityName);
                amenity.setActive(true);
                amenity.setCreatedAt(LocalDateTime.now());
                newAmenities.add(amenity);
            }
            roomAmenityRepository.saveAll(newAmenities);
        }

        return true;
    }

    @Override
    public Boolean addRoom(AddRoom addRoom) {
        Optional<Room> existingRoom = roomRepository.findByHotelidAndRoomNumber(addRoom.hotelId, addRoom.roomNumber);
        if (existingRoom.isEmpty()) {
            Room room = new Room();
            room.setHotelid(addRoom.hotelId);
            room.setRoomNumber(addRoom.roomNumber);
            room.setRoomType(addRoom.roomType);
            room.setPrice_per_night(addRoom.price);
            room.setCapacity(addRoom.capacity);
            room.setDescription(addRoom.description);
            room.setCreated_at(LocalDateTime.now());
            room.setAvailable(true);
            room.setActive(true);
            Room roomNew = roomRepository.save(room);

            if (addRoom.amenities != null && !addRoom.amenities.isEmpty()) {
                List<RoomAmenity> amenities = new ArrayList<>();
                for (String amenityName : addRoom.amenities) {
                    RoomAmenity amenity = new RoomAmenity();
                    amenity.setRoom(roomNew);
                    amenity.setAmenityName(amenityName);
                    amenity.setActive(true);
                    amenity.setCreatedAt(LocalDateTime.now());
                    amenities.add(amenity);
                }
                roomAmenityRepository.saveAll(amenities);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Boolean activeRoom(UpdateActive updateActive) {
        Optional<Room> roomOptional = roomRepository.findById(updateActive.roomId);
        if (roomOptional.isPresent()) {
            Room room = roomOptional.get();
            room.setActive(updateActive.isActive);
            roomRepository.save(room);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Boolean updateAvailability(UpdateAvailability updateAvailability) {
        Optional<Room> roomOptional = roomRepository.findById(updateAvailability.roomId);
        if (roomOptional.isPresent()) {
            Room room = roomOptional.get();
            room.setAvailable(updateAvailability.isAvailable);
            room.setUpdated_at(LocalDateTime.now());
            roomRepository.save(room);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<Rooms> getAllRooms(int userId) {
        Map<String, Object> response = hotelServiceClient.checkHotelId(userId);
        int hotelId = (int) response.get("hotelId");
        List<Room> roomList = roomRepository.findByHotelid(hotelId);
        List<Rooms> roomsResponse = new ArrayList<>();
        for (Room room : roomList) {
            Rooms roomDto = new Rooms();
            roomDto.roomId = room.getId();
            roomDto.roomNumber = room.getRoomNumber();
            roomDto.roomType = room.getRoomType();
            roomDto.pricePerNight = room.getPrice_per_night();
            roomDto.capacity = room.getCapacity();
            roomDto.description = room.getDescription();
            roomDto.isAvailable = room.isAvailable();
            roomDto.isActive = room.isActive();
            roomsResponse.add(roomDto);
        }
        return roomsResponse;
    }

    @Override
    public Rooms getRoomDetail(int roomId) {
        Optional<Room> roomOptional = roomRepository.findById(roomId);
        if (roomOptional.isPresent()) {
            Room room = roomOptional.get();
            Rooms roomDto = new Rooms();
            roomDto.roomId = room.getId();
            roomDto.roomNumber = room.getRoomNumber();
            roomDto.roomType = room.getRoomType();
            roomDto.pricePerNight = room.getPrice_per_night();
            roomDto.capacity = room.getCapacity();
            roomDto.description = room.getDescription();
            roomDto.isAvailable = room.isAvailable();
            roomDto.isActive = room.isActive();

            List<RoomAmenity> amenities = roomAmenityRepository.findByRoom_Id(roomId);
            if (amenities != null && !amenities.isEmpty()) {
                String amenityNames = amenities.stream()
                        .map(RoomAmenity::getAmenityName)
                        .collect(Collectors.joining(", "));
                roomDto.amenities = amenityNames;
            }

            return roomDto;
        }
        return null;
    }
}
