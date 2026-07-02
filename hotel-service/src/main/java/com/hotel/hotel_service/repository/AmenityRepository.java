package com.hotel.hotel_service.repository;

import com.hotel.hotel_service.model.Amenities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import java.util.List;

public interface AmenityRepository extends JpaRepository<Amenities,Integer> {
    List<Amenities> findById(int id);

    List<Amenities> findAnimitiesById(int id);

    @Query("SELECT a FROM Amenities a WHERE a.amenity_name = :name")
    Optional<Amenities> findByName(@Param("name") String name);
}
