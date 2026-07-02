package com.hotel.hotel_service.repository;

import com.hotel.hotel_service.model.Home;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HomeRepository extends JpaRepository<Home,Integer> {
    List<Home> findByCityContainingIgnoreCase(String city);

    Optional<Home> findByOwnerId(int ownerId);

    List<Home> findByRatingGreaterThan(double ratingIsGreaterThan);
}
