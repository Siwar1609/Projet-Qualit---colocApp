package org.example.pfabackend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

public record ColocationDTO(
        Long id,

        @NotNull(message = "Name cannot be null")
        @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
        String name,

        String idOfPublisher,

        String nameOfPublisher,

        @NotNull(message = "Address cannot be null")
        String address,

        @NotNull(message = "City cannot be null")
        String city,

        @NotNull(message = "Postal Code cannot be null")
        String postalCode,

        String description,

        @NotNull(message = "Price cannot be null")
        @PositiveOrZero(message = "Price must be 0 or greater")
        Double price,


        @NotNull(message = "Number of rooms is required")
        @Min(value = 1, message = "Must have at least 1 room")
        Integer numberOfRooms,

        String roommatesGenderPreference,

        Boolean hasWifi,
        Boolean hasParking,
        Boolean hasAirConditioning,
        Boolean isFurnished,
        Boolean hasBalcony,
        Boolean hasPrivateBathroom,

        Integer maxRoommates,
        Integer currentRoommates,

        String status, // e.g., Available, Occupied, Pending

        List<String> rules,
        List<String> tags,

        List<String> imageUrls, // Extracted image URLs
        Double averageRating, // read-only

        List<ReviewDTO> reviews, // optional for display

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime availableFrom,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt,

        Boolean isArchived,
        Boolean isPublished
) {
    public ColocationDTO {
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
    }
}
