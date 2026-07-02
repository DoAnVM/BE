package com.review.review_service.repository;

import com.review.review_service.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment,Integer>
{
    List<Comment> findByHotelId(int hotelId);
}
