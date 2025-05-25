package org.example.pfabackend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReviewDTO(
        Long id,
        String reviewerId,
        String reviewerName,
        int rating,
        String comment,
        LocalDate createdAt
) {
}
