package com.hotel.room_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "rooms")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "hotel_id", nullable = false)
    private int hotelid;

    @Column(name = "room_number", nullable = true)
    private String roomNumber;

    @Column(name = "room_type", nullable = true)
    private String roomType;

    @Column(name = "price_per_night", nullable = false)
    private double price_per_night;

    @Column(name = "capacity", nullable = true)
    private int capacity;

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "is_available", nullable = true)
    private boolean available;

    @Column(name = "created_at", nullable = true)
    private LocalDateTime created_at;

    @Column(name = "updated_at", nullable = true)
    private LocalDateTime updated_at;

    @Column(name = "is_active", nullable = true)
    private boolean active;
}
