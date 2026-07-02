package com.review.review_service.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public class CommentByHotelId {
    public int commentId;
    public String customerName;
    public String comment;
    public LocalDateTime createdAt;
    public String hotelName;
    public int star;
}
