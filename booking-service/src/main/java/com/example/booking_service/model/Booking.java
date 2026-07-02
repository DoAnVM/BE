package com.example.booking_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private int id;

    @Column(name = "customer_id", nullable = false)
    private int customerId;

    @Column(name = "hotel_id", nullable = false)
    private int hotelId;

    @Column(name = "check_in_date", nullable = false)
    private Date checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private Date checkOutDate;

    @Column(name = "guests", nullable = false)
    private int guests;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingType bookingType;

    @Column(name = "room_type", nullable = false)
    private String roomType;

    @Column(name = "total_room", nullable = false)
    private int totalRoom;

    @Column(name = "created_at", nullable = true)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = true)
    private LocalDateTime updatedAt;

    @Column(name = "fee", nullable = true)
    private float fee;
}
