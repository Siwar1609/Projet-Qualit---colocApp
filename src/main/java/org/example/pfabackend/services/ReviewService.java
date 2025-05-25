package org.example.pfabackend.services;

import org.example.pfabackend.dto.ReviewDTO;

import java.util.List;

public interface ReviewService {
    ReviewDTO addReview(Long colocationId, ReviewDTO reviewDTO, String userId, String username);

    List<ReviewDTO> getReviewsByColocationId(Long colocationId);
}

