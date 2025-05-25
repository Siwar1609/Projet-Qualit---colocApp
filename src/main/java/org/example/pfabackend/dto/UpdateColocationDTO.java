package org.example.pfabackend.dto;


import java.time.LocalDate;
import java.util.List;

public record UpdateColocationDTO(
        String name,
        String description,
        String address,
        String city,
        String postalCode,
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
        List<ReviewDTO> reviews,
        LocalDate availableFrom,
        LocalDate updatedAt,
        Boolean isArchived,
        Boolean isPublished
) {}
