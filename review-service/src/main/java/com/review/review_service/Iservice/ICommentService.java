package com.review.review_service.Iservice;

import com.review.review_service.dto.request.AddComment;
import com.review.review_service.dto.response.CommentByHotelId;

import java.util.List;

public interface ICommentService {
    boolean createComment(int customerId, AddComment addComment);
    boolean deleteComment(int commentId);
    List<CommentByHotelId> getCommentsByOwnerId(int ownerId);
    List<CommentByHotelId> getCommentsByHotelId(int hotelId);
    List<CommentByHotelId> getAllComments();
    Float getAverageStarByHotelId(int hotelId);
    Integer countCommentByHotelId(int hotelId);
}
