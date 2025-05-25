package org.example.pfabackend.services.implementations;

import lombok.RequiredArgsConstructor;
import org.example.pfabackend.dto.ReviewDTO;
import org.example.pfabackend.entities.Colocation;
import org.example.pfabackend.entities.Review;
import org.example.pfabackend.repositories.ColocationRepository;
import org.example.pfabackend.repositories.ReviewRepository;
import org.example.pfabackend.services.ReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ColocationRepository colocationRepository;

    @Override
    @Transactional
    public ReviewDTO addReview(Long colocationId, ReviewDTO reviewDTO, String userId, String username) {

        Colocation colocation = colocationRepository.findById(colocationId)
                .orElseThrow(() -> new IllegalArgumentException("Colocation not found"));

        if (!colocation.getAssignedUserIds().contains(userId)) {
            throw new IllegalStateException("Only assigned users can add reviews");
        }

        Review review = new Review();
        review.setReviewerId(userId);
        review.setReviewerName(username); // now set from token
        review.setRating(reviewDTO.rating());
        review.setComment(reviewDTO.comment());
        review.setColocation(colocation);

        Review saved = reviewRepository.save(review);

        return new ReviewDTO(
                saved.getId(),
                saved.getReviewerId(),
                saved.getReviewerName(),
                saved.getRating(),
                saved.getComment(),
                saved.getCreatedAt()
        );
    }

    @Override
    public List<ReviewDTO> getReviewsByColocationId(Long colocationId) {
        List<Review> reviews = reviewRepository.findByColocationId(colocationId);
        return reviews.stream()
                .map(review -> new ReviewDTO(
                        review.getId(),
                        review.getReviewerId(),
                        review.getReviewerName(),
                        review.getRating(),
                        review.getComment(),
                        review.getCreatedAt()
                ))
                .toList();
    }


}
