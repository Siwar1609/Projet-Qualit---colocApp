package org.example.pfabackend.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record CreateColocationDTO(
        @NotBlank String name,
        @NotBlank String address,
        String city,
        String postalCode,
        String description,
        Double price,
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
        Double averageRating,
        List<String> reviews,
        LocalDate availableFrom,
        LocalDate createdAt,
        LocalDate updatedAt,
        Boolean isArchived,
        Boolean isPublished,
        List<String> assignedUserIds
) {}
