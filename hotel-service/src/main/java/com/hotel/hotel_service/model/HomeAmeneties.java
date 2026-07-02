package com.hotel.hotel_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "home_amenities")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HomeAmeneties {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "home_id", nullable = false)
    private Home home;

    @ManyToOne
    @JoinColumn(name = "amenity_id", nullable = false)
    private Amenities amenity;
}
