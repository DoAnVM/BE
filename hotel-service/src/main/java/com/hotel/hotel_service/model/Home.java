package com.hotel.hotel_service.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@Table(name = "homes")
@Entity
@AllArgsConstructor
@NoArgsConstructor

public class Home {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "owner_id", nullable = false)
    private int ownerId;

    @Column(name = "home_name", nullable = false)
    private String home_name;

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "price_per_night", nullable = false)
    private double price_per_night;

    @Column(name = "street", nullable = true)
    private String street;

    @Column(name = "district", nullable = true)
    private String district;

    @Column(name = "city", nullable = true)
    private String city;

    @Column(name = "country", nullable = true)
    private String country;

    @Column(name = "rating", nullable = true)
    private double rating;

    @Column(name = "available", nullable = true)
    private boolean available;

    @Column(name = "created_at", nullable = true)
    private LocalDateTime created_at;

    @Column(name = "updated_at", nullable = true)
    private LocalDateTime updated_at;
}
