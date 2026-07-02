package com.payment_service.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {
    private Long bookingId;
    private Long amount;
    private String description;
    private String checkInDate;
    private String checkOutDate;
    private String customerEmail;
    private Long customerId;
    private String customerName;
    private String hotelName;
    private String roomType;
}
