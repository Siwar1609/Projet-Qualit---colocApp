package org.example.pfabackend.mappers;

import org.example.pfabackend.dto.ColocationDTO;
import org.example.pfabackend.dto.ReviewDTO;
import org.example.pfabackend.entities.Colocation;
import org.example.pfabackend.entities.ColocationImage;
import org.example.pfabackend.entities.Review;
import org.example.pfabackend.services.implementations.ColocationServiceImpl;

import java.util.List;

public class ColocationMapper {

    public static Colocation toEntity(ColocationDTO dto) {
        Colocation colocation = new Colocation();

        colocation.setId(dto.id());
        colocation.setName(dto.name());
        colocation.setIdOfPublisher(dto.idOfPublisher());
        colocation.setNameOfPublisher(dto.nameOfPublisher());
        colocation.setAddress(dto.address());
        colocation.setCity(dto.city());
        colocation.setPostalCode(dto.postalCode());
        colocation.setDescription(dto.description());
        colocation.setPrice(dto.price());
        colocation.setAvailableFrom(dto.availableFrom());
        colocation.setNumberOfRooms(dto.numberOfRooms());
        colocation.setRoommatesGenderPreference(dto.roommatesGenderPreference());
        colocation.setHasWifi(dto.hasWifi());
        colocation.setHasParking(dto.hasParking());
        colocation.setHasAirConditioning(dto.hasAirConditioning());
        colocation.setIsFurnished(dto.isFurnished());
        colocation.setHasBalcony(dto.hasBalcony());
        colocation.setHasPrivateBathroom(dto.hasPrivateBathroom());
        colocation.setMaxRoommates(dto.maxRoommates());
        colocation.setCurrentRoommates(dto.currentRoommates());
        colocation.setStatus(dto.status());
        colocation.setRules(dto.rules());
        colocation.setTags(dto.tags());
        colocation.setIsArchived(dto.isArchived() != null ? dto.isArchived() : false);
        colocation.setIsPublished(dto.isPublished() != null ? dto.isPublished() : false);

        // Convert image URLs to ColocationImage entities
        ColocationServiceImpl.imageUpdate(dto, colocation);

        // Convert reviews DTOs to entities
        if (dto.reviews() != null) {
            List<Review> reviews = dto.reviews().stream()
                    .map(ReviewMapper::toEntity) // tu dois créer ReviewMapper aussi
                    .toList();
            colocation.setReviews(reviews);
        }

        return colocation;
    }

    public static ColocationDTO toDto(Colocation colocation) {
        List<String> imageUrls = colocation.getImages().stream()
                .map(ColocationImage::getUrl)
                .toList();

        List<ReviewDTO> reviews = colocation.getReviews() != null
                ? colocation.getReviews().stream()
                .map(ReviewMapper::toDto)
                .toList()
                : List.of();

        return new ColocationDTO(
                colocation.getId(),
                colocation.getName(),
                colocation.getIdOfPublisher(),
                colocation.getNameOfPublisher(),
                colocation.getAddress(),
                colocation.getCity(),
                colocation.getPostalCode(),
                colocation.getDescription(),
                colocation.getPrice(),
                colocation.getNumberOfRooms(),
                colocation.getRoommatesGenderPreference(),
                colocation.getHasWifi(),
                colocation.getHasParking(),
                colocation.getHasAirConditioning(),
                colocation.getIsFurnished(),
                colocation.getHasBalcony(),
                colocation.getHasPrivateBathroom(),
                colocation.getMaxRoommates(),
                colocation.getCurrentRoommates(),
                colocation.getStatus(),
                colocation.getRules(),
                colocation.getTags(),
                imageUrls,
                colocation.getAverageRating(),
                reviews,
                colocation.getAvailableFrom(),
                colocation.getCreatedAt(),
                colocation.getUpdatedAt(),
                colocation.getIsArchived(),
                colocation.getIsPublished()
        );
    }
}
