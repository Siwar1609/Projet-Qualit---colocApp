package org.example.pfabackend.services.implementations;

import org.example.pfabackend.dto.ReviewDTO;
import org.example.pfabackend.entities.Colocation;
import org.example.pfabackend.dto.ColocationDTO;
import org.example.pfabackend.entities.ColocationImage;
import org.example.pfabackend.exceptions.ResourceNotFoundException;
import org.example.pfabackend.repositories.ColocationRepository;
import org.example.pfabackend.security.JwtConverter;
import org.example.pfabackend.services.ColocationService;
import org.springframework.data.domain.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static org.example.pfabackend.security.SecurityConfig.ADMIN;

@Service
public class ColocationServiceImpl implements ColocationService {
    private final ColocationRepository colocationRepository;
    private final JwtConverter jwtConverter;

    public ColocationServiceImpl(ColocationRepository colocationRepository, JwtConverter jwtConverter) {
        this.colocationRepository = colocationRepository;
        this.jwtConverter = jwtConverter;
    }

    @Override
    public Page<ColocationDTO> getAllColocations(String search, int page, int size, Jwt jwt) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        boolean isAdmin = jwt != null && jwtConverter.hasRole(jwt, ADMIN);

        Page<Colocation> result;

        if (isAdmin) {
            result = (search == null || search.trim().isEmpty())
                    ? colocationRepository.findAll(pageable)
                    : colocationRepository.search(search, pageable);
        } else {
            result = (search == null || search.trim().isEmpty())
                    ? colocationRepository.findByIsPublishedTrueAndIsArchivedFalse(pageable)
                    : colocationRepository.searchPublic(search, pageable);
        }

        return result.map(this::convertToDTO);
    }

    @Override
    public Optional<ColocationDTO> getColocationById(Long id, Jwt jwt) {
        boolean isAdmin = jwt != null && jwtConverter.hasRole(jwt, ADMIN);

        Optional<Colocation> colocation = isAdmin
                ? colocationRepository.findById(id)
                : colocationRepository.findByIdAndIsPublishedTrueAndIsArchivedFalse(id);

        return colocation.map(this::convertToDTO);
    }

    @Override
    public ColocationDTO saveColocation(ColocationDTO colocationDTO) {
        Colocation colocation = convertToEntity(colocationDTO);
        return convertToDTO(colocationRepository.save(colocation));
    }

    @Override
    public ColocationDTO updateColocation(Long id, ColocationDTO colocationDTO) {
        Colocation colocation = colocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Colocation with ID " + id + " not found"));

        colocation.setName(colocationDTO.name());
        colocation.setNameOfPublisher(colocationDTO.nameOfPublisher());
        colocation.setIdOfPublisher(colocationDTO.idOfPublisher());
        colocation.setDescription(colocationDTO.description());
        colocation.setAddress(colocationDTO.address());
        colocation.setPrice(colocationDTO.price());

        return convertToDTO(colocationRepository.save(colocation));
    }

    @Override
    public void deleteColocation(Long id) {
        if (!colocationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Colocation with ID " + id + " not found");
        }
        colocationRepository.deleteById(id);
    }

    // Convert Colocation Entity to ColocationDTO
    private ColocationDTO convertToDTO(Colocation colocation) {
        List<String> imageUrls = colocation.getImages().stream()
                .map(ColocationImage::getUrl)
                .toList();

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
                colocation.getNumberOfRooms(), // ✅ Corrigé ici
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
                colocation.getReviews() != null
                        ? colocation.getReviews().stream().map(review -> new ReviewDTO(
                        review.getId(),
                        review.getReviewerId(),
                        review.getReviewerName(),
                        review.getRating(),
                        review.getComment(),
                        review.getCreatedAt()
                )).toList()
                        : List.of(),
                colocation.getAvailableFrom(), // ✅ Maintenant à la bonne place
                colocation.getCreatedAt(),
                colocation.getUpdatedAt(),
                colocation.getIsArchived(),
                colocation.getIsPublished()
        );
    }



    // Convert ColocationDTO to Colocation Entity
    private Colocation convertToEntity(ColocationDTO dto) {
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

        // Convert imageUrls (DTO) to List<ColocationImage> (Entity)
        if (dto.imageUrls() != null) {
            List<ColocationImage> imageEntities = dto.imageUrls().stream()
                    .map(url -> {
                        ColocationImage image = new ColocationImage();
                        image.setUrl(url);
                        image.setColocation(colocation); // important to set the owning side
                        return image;
                    })
                    .toList();
            colocation.setImages(imageEntities);
        }

        // averageRating and reviews are managed separately (read-only or related entity)
        return colocation;
    }


    /**
     * Update the isPublished status of a Colocation
     */
    public ColocationDTO updateIsPublished(Long id, boolean isPublished) {
        Colocation colocation = colocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Colocation with ID " + id + " not found"));

        colocation.setIsPublished(isPublished); // Update the status

        // Save and return the updated Colocation as a DTO
        return convertToDTO(colocationRepository.save(colocation));
    }

    /**
     * Update the isArchived status of a Colocation
     */
    public ColocationDTO updateIsArchived(Long id, boolean isArchived) {
        Colocation colocation = colocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Colocation with ID " + id + " not found"));

        colocation.setIsArchived(isArchived); // Update the status

        // Save and return the updated Colocation as a DTO
        return convertToDTO(colocationRepository.save(colocation));
    }
}
