package com.review.review_service.controller;

import com.review.review_service.dto.request.AddComment;
import com.review.review_service.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/comment")
    public ResponseEntity<Map<String, Object>> addComment(@RequestHeader("X-Auth-UserId") String customerIdstr,
                                                          @RequestBody AddComment addComment){
        Map<String, Object> response = new HashMap<>();
        try{
            int customerId = Integer.parseInt(customerIdstr);
            boolean isCreated = commentService.createComment(customerId, addComment);
            if(isCreated){
                response.put("message", "Comment added successfully");
            } else {
                response.put("message", "Failed to add comment");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e){
            response.put("message", "Error occurred while adding comment");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/comment")
    public ResponseEntity<Map<String, Object>> getCommentsByHotelId(@RequestParam int hotelId){
        Map<String, Object> response = new HashMap<>();
        try {
            var comments = commentService.getCommentsByHotelId(hotelId);
            if (comments != null) {
                response.put("comments", comments);
                response.put("message", "Comments retrieved successfully");
            } else {
                response.put("message", "No comments found for this hotel");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Error occurred while retrieving comments");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/comments")
    public ResponseEntity<Map<String, Object>> getCommentsByOwnerId(@RequestHeader("X-Auth-UserId") String ownerIdstr) {
        Map<String, Object> response = new HashMap<>();
        try {
            int ownerId = Integer.parseInt(ownerIdstr);
            var comments = commentService.getCommentsByOwnerId(ownerId);
            if (comments != null) {
                response.put("comments", comments);
                response.put("message", "Comments retrieved successfully");
            } else {
                response.put("message", "No comments found for this hotel");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Error occurred while retrieving comments");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/comment/all")
    public ResponseEntity<Map<String, Object>> getAllComments(){
        Map<String, Object> response = new HashMap<>();
        try {
            var comments = commentService.getAllComments();
            if (comments != null) {
                response.put("comments", comments);
                response.put("message", "All comments retrieved successfully");
            } else {
                response.put("message", "No comments found");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Error occurred while retrieving comments");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @DeleteMapping("/comment")
    public ResponseEntity<Map<String, Object>> deleteComment(@RequestParam int commentId){
        Map<String, Object> response = new HashMap<>();
        try{
            boolean isDeleted = commentService.deleteComment(commentId);
            if(isDeleted){
                response.put("message", "Comment deleted successfully");
            } else {
                response.put("message", "Failed to delete comment");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e){
            response.put("message", "Error occurred while deleting comment");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/averageStar")
    public ResponseEntity<Map<String, Object>> getAverageStarByHotelId(@RequestParam int hotelId){
        Map<String, Object> response = new HashMap<>();
        try {
            Float averageStar = commentService.getAverageStarByHotelId(hotelId);
            response.put("averageStar", averageStar);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Error occurred while retrieving average star");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/countComment")
    public ResponseEntity<Map<String, Object>> countCommentByHotelId(@RequestParam int hotelId){
        Map<String, Object> response = new HashMap<>();
        try {
            int countComment = commentService.countCommentByHotelId(hotelId);
            response.put("countComment", countComment);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Error occurred while counting comment");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
