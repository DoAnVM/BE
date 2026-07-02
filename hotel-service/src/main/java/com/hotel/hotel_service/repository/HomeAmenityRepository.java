package com.hotel.hotel_service.repository;

import com.hotel.hotel_service.model.HomeAmeneties;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HomeAmenityRepository extends JpaRepository<HomeAmeneties, Integer> {
    List<HomeAmeneties> findHomeAmenetiesByHome_Id(int homeId);
}
