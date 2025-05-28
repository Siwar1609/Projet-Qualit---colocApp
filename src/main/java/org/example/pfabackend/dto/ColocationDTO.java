package org.example.pfabackend.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public record ColocationDTO(
        Long id,

        @NotNull(message = "Name cannot be null")
        @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
        String name,

        @NotBlank(message = "Publisher ID cannot be blank")
        String idOfPublisher,

        @NotBlank(message = "Publisher name cannot be blank")
        String nameOfPublisher,

        @NotNull(message = "Address cannot be null")
        @Size(min = 3, message = "Address must be at least 3 characters")
        String address,

        @NotNull(message = "City cannot be null")
        String city,

        @NotNull(message = "Postal Code cannot be null")
        String postalCode,

        String description,

        @NotNull(message = "Price cannot be null")
        @PositiveOrZero(message = "Price must be 0 or greater")
        Double price,

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

        String status,

        List<String> rules,
        List<String> tags,

        List<String> imageUrls,

        Double averageRating, // calculé à partir des reviews

        List<ReviewDTO> reviews,

        LocalDate availableFrom,

        LocalDate createdAt,

        LocalDate updatedAt,

        Boolean isArchived,
        Boolean isPublished
) {
    public ColocationDTO {
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }

        if (numberOfRooms != null && numberOfRooms < 1) {
            throw new IllegalArgumentException("Must have at least 1 room");
        }
    }
    public String getIdOfPublisher() {
        return idOfPublisher != null ? idOfPublisher : "";
    }
}
