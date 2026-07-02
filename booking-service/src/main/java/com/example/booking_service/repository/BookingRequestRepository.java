package com.example.booking_service.repository;

import com.example.booking_service.model.BookingRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRequestRepository extends JpaRepository<BookingRequest, String> {
}
