package org.example.pfabackend.controllers;

import lombok.RequiredArgsConstructor;
import org.example.pfabackend.dto.ReviewDTO;
import org.example.pfabackend.services.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/colocations/{colocationId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewDTO> addReview(
            @PathVariable Long colocationId,
            @RequestBody ReviewDTO reviewDTO,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getClaimAsString("sub");
        String username = jwt.getClaimAsString("preferred_username");

        ReviewDTO result = reviewService.addReview(colocationId, reviewDTO, userId, username);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<ReviewDTO>> getReviewsByColocationId(@PathVariable Long colocationId) {
        List<ReviewDTO> reviews = reviewService.getReviewsByColocationId(colocationId);
        return ResponseEntity.ok(reviews);
    }

}
