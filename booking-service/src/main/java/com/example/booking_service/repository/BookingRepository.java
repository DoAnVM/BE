package com.example.booking_service.repository;

import com.example.booking_service.model.Booking;
import com.example.booking_service.model.BookingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findByCustomerId(int customerId);

    @Query("SELECT b FROM Booking b WHERE b.hotelId = :hotelId AND " +
           "((b.checkInDate <= :checkOutDate AND b.checkOutDate >= :checkInDate))")
    List<Booking> findConflictingBookings(
        @Param("hotelId") int hotelId,
        @Param("checkInDate") Date checkInDate,
        @Param("checkOutDate") Date checkOutDate
    );

    @Query("""
            SELECT b FROM Booking b
            WHERE b.customerId = :customerId
              AND b.hotelId = :hotelId
              AND b.checkInDate = :checkInDate
              AND b.checkOutDate = :checkOutDate
              AND b.guests = :guests
              AND b.bookingType = :bookingType
              AND b.createdAt >= :fromTime
            ORDER BY b.createdAt DESC, b.id DESC
            """)
    List<Booking> findRecentSameRequestBookings(
            @Param("customerId") int customerId,
            @Param("hotelId") int hotelId,
            @Param("checkInDate") Date checkInDate,
            @Param("checkOutDate") Date checkOutDate,
            @Param("guests") int guests,
            @Param("bookingType") BookingType bookingType,
            @Param("fromTime") LocalDateTime fromTime
    );

    @Query("""
            SELECT b FROM Booking b
            WHERE b.customerId = :customerId
              AND b.hotelId = :hotelId
              AND b.bookingType IN ('CONFIRMED', 'CHECKED_IN')
              AND b.checkInDate < :checkOutDate
              AND b.checkOutDate > :checkInDate
            """)
    List<Booking> findOverlappingActiveBookings(
            @Param("customerId") int customerId,
            @Param("hotelId") int hotelId,
            @Param("checkInDate") Date checkInDate,
            @Param("checkOutDate") Date checkOutDate
    );

    @Query("""
            SELECT b FROM Booking b
            WHERE b.hotelId = :hotelId
              AND UPPER(b.roomType) = UPPER(:roomType)
              AND b.bookingType IN ('CONFIRMED', 'CHECKED_IN')
              AND b.checkInDate < :checkOutDate
              AND b.checkOutDate > :checkInDate
            """)
    List<Booking> findBookedRoomsForDateRange(
            @Param("hotelId") int hotelId,
            @Param("roomType") String roomType,
            @Param("checkInDate") Date checkInDate,
            @Param("checkOutDate") Date checkOutDate
    );

    List<Booking> findByHotelId(int hotelId);
}
