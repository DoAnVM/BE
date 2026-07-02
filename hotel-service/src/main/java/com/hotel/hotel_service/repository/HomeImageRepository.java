package com.hotel.hotel_service.repository;

import com.hotel.hotel_service.model.HomeImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HomeImageRepository extends JpaRepository<HomeImage, Integer>
{
    List<HomeImage> findHomeImageByHome_Id(int homeId);
}
