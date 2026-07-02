package com.review.review_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "comment")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "customer_id", nullable = true)
    private int customerId;

    @Column(name = "hotel_id", nullable = true)
    private int hotelId;

    @Column(name = "comment", nullable = true, length = 1000)
    private String comment;

    @Column(name = "created_at", nullable = true)
    private LocalDateTime createdAt;

    @Column(name = "star", nullable = true)
    private int star;
}
