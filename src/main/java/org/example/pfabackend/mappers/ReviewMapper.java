package org.example.pfabackend.mappers;

import org.example.pfabackend.dto.ReviewDTO;
import org.example.pfabackend.entities.Review;

public class ReviewMapper {

    public static ReviewDTO toDto(Review entity) {
        return new ReviewDTO(
                entity.getId(),
                entity.getReviewerId(),
                entity.getReviewerName(),
                entity.getRating(),
                entity.getComment(),
                entity.getCreatedAt()
        );
    }

    public static Review toEntity(ReviewDTO dto) {
        Review review = new Review();
        review.setId(dto.id());
        review.setReviewerId(dto.reviewerId());
        review.setReviewerName(dto.reviewerName());
        review.setComment(dto.comment());
        review.setRating(dto.rating());
        review.setCreatedAt(dto.createdAt());
        return review;
    }
}
