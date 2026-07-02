package com.review.review_service.service;

import com.review.review_service.Iservice.ICommentService;
import com.review.review_service.client.HotelServiceClient;
import com.review.review_service.client.UserServiceClient;
import com.review.review_service.dto.request.AddComment;
import com.review.review_service.dto.response.CommentByHotelId;
import com.review.review_service.model.Comment;
import com.review.review_service.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService implements ICommentService {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserServiceClient userServiceClient;
    @Autowired
    private HotelServiceClient hotelServiceClient;

    @Override
    public boolean createComment(int customerId, AddComment addComment) {
        Comment comment = new Comment();
        comment.setComment(addComment.comment);
        comment.setCustomerId(customerId);
        comment.setHotelId(addComment.hotelId);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setStar(addComment.star);
        commentRepository.save(comment);
        return true;
    }

    @Override
    public boolean deleteComment(int commentId) {
        Optional<Comment> comment = commentRepository.findById(commentId);
        if (comment.isPresent()) {
            commentRepository.deleteById(commentId);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<CommentByHotelId> getCommentsByOwnerId(int ownerId) {
        Map<String, Object> response = hotelServiceClient.checkHotelId(ownerId);
        int hotelId = (int) response.get("hotelId");

        List<Comment> comments = commentRepository.findByHotelId(hotelId);
        if (comments.isEmpty())
        {
            return null;
        }
        List<CommentByHotelId> commentsByHotelId = new ArrayList<>();
        for (Comment comment : comments) {
            Map<String,Object> userResponse = userServiceClient.Customer(comment.getCustomerId());
            String customerName = (String) userResponse.get("fullName");
            CommentByHotelId commentByHotelId = CommentByHotelId.builder()
                    .commentId(comment.getId())
                    .customerName(customerName)
                    .comment(comment.getComment())
                    .createdAt(comment.getCreatedAt())
                    .star(comment.getStar())
                    .build();
            commentsByHotelId.add(commentByHotelId);
        }
        return commentsByHotelId;
    }

    @Override
    public List<CommentByHotelId> getCommentsByHotelId(int hotelId) {
        List<Comment> comments = commentRepository.findByHotelId(hotelId);
        if (comments.isEmpty())
        {
            return null;
        }
        List<CommentByHotelId> commentsByHotelId = new ArrayList<>();
        for (Comment comment : comments) {
            Map<String,Object> userResponse = userServiceClient.Customer(comment.getCustomerId());
            String customerName = (String) userResponse.get("fullName");
            CommentByHotelId commentByHotelId = CommentByHotelId.builder()
                    .commentId(comment.getId())
                    .customerName(customerName)
                    .comment(comment.getComment())
                    .createdAt(comment.getCreatedAt())
                    .star(comment.getStar())
                    .build();
            commentsByHotelId.add(commentByHotelId);
        }
        return commentsByHotelId;
    }

    @Override
    public List<CommentByHotelId> getAllComments() {
        List<Comment> comments = commentRepository.findAll();
        if (comments.isEmpty())
        {
            return null;
        }
        List<CommentByHotelId> commentsByHotelId = new ArrayList<>();
        for (Comment comment : comments) {
            Map<String,Object> customerResponse = userServiceClient.Customer(comment.getCustomerId());
            String customerName = (String) customerResponse.get("fullName");

            Map<String,Object> hotelResponse = hotelServiceClient.getHotelDetails(comment.getHotelId());
            String hotelName = (String) hotelResponse.get("hotelName");

            CommentByHotelId commentByHotelId = CommentByHotelId.builder()
                    .commentId(comment.getId())
                    .customerName(customerName)
                    .hotelName(hotelName)
                    .comment(comment.getComment())
                    .createdAt(comment.getCreatedAt())
                    .star(comment.getStar())
                    .build();
            commentsByHotelId.add(commentByHotelId);
        }
        return commentsByHotelId;
    }

    @Override
    public Float getAverageStarByHotelId(int hotelId) {
        List<Comment> comments = commentRepository.findByHotelId(hotelId);
        if (comments.isEmpty()) {
            return 0f;
        }
        float totalStars = 0f;
        for (Comment comment : comments) {
            totalStars += comment.getStar();
        }
        return totalStars / comments.size();
    }

    @Override
    public Integer countCommentByHotelId(int hotelId) {
        List<Comment> comments = commentRepository.findByHotelId(hotelId);
        return comments.size();
    }
}
