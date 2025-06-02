package org.example.pfabackend.services.implementations;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.example.pfabackend.dto.CreateColocationDTO;
import org.example.pfabackend.dto.ReviewDTO;
import org.example.pfabackend.dto.UpdateColocationDTO;
import org.example.pfabackend.entities.Colocation;
import org.example.pfabackend.dto.ColocationDTO;
import org.example.pfabackend.entities.ColocationImage;
import org.example.pfabackend.entities.Review;
import org.example.pfabackend.exception.ColocationException;
import org.example.pfabackend.exceptions.ResourceNotFoundException;
import org.example.pfabackend.mappers.ReviewMapper;
import org.example.pfabackend.repositories.ColocationRepository;
import org.example.pfabackend.security.JwtConverter;
import org.example.pfabackend.services.ColocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

import static org.example.pfabackend.security.SecurityConfig.ADMIN;
@Service
public class ColocationServiceImpl implements ColocationService {
    private final ColocationRepository colocationRepository;
    private final JwtConverter jwtConverter;
    private final UserService userService;
    private final Cloudinary cloudinary;


    @Autowired
    public ColocationServiceImpl(
            ColocationRepository colocationRepository,
            JwtConverter jwtConverter,
            UserService userService,
            Cloudinary cloudinary) {
        this.colocationRepository = colocationRepository;
        this.jwtConverter = jwtConverter;
        this.userService = userService;
        this.cloudinary = cloudinary;
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
        assert jwt != null;
        String userId = jwt.getClaimAsString("sub");

        System.out.println("Gerrrrrr");
        System.out.println(isAdmin);
        Optional<Colocation> colocation = isAdmin
                ? colocationRepository.findById(id)
                : Optional.ofNullable(colocationRepository
                .getByIdVisibleToUser(id, userId)
                .orElseThrow(() -> new AccessDeniedException("Vous n'avez pas accès à cette colocation.")));


        return colocation.map(this::convertToDTO);
    }

    @Override
    public ColocationDTO saveColocation(ColocationDTO colocationDTO) {
        try {
            // Vérifier la validité du DTO
            if (colocationDTO == null) {
                throw new IllegalArgumentException("ColocationDTO cannot be null");
            }

            // Convertir le DTO en entité (Colocation)
            Colocation colocation = convertToEntity(colocationDTO);

            // Sauvegarder l'entité dans la base de données
            Colocation savedColocation = colocationRepository.save(colocation);

            // Convertir l'entité sauvegardée en DTO
            return convertToDTO(savedColocation);

        } catch (IllegalArgumentException ex) {
            // Logique pour gérer les erreurs liées aux arguments invalides
            throw new ColocationException("Invalid input data: " + ex.getMessage(), ex);
        } catch (ValidationException ex) {
            // Gérer les erreurs de validation
            throw new ColocationException("Validation failed: " + ex.getMessage(), ex);
        } catch (DataIntegrityViolationException ex) {
            // Gérer les violations d'intégrité de la base de données (par exemple, contraintes de clé unique)
            throw new ColocationException("Database integrity error: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            // Gérer toutes les autres exceptions génériques
            throw new ColocationException("An unexpected error occurred: " + ex.getMessage(), ex);
        }
    }

    @Override
    public ColocationDTO updateColocation(Long id, UpdateColocationDTO dto) {
        Colocation colocation = colocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Colocation with ID " + id + " not found"));

        if (dto.name() != null) colocation.setName(dto.name());
        if (dto.description() != null) colocation.setDescription(dto.description());
        if (dto.address() != null) colocation.setAddress(dto.address());
        if (dto.city() != null) colocation.setCity(dto.city());
        if (dto.postalCode() != null) colocation.setPostalCode(dto.postalCode());
        if (dto.price() != null) colocation.setPrice(dto.price());
        if (dto.numberOfRooms() != null) colocation.setNumberOfRooms(dto.numberOfRooms());
        if (dto.roommatesGenderPreference() != null) colocation.setRoommatesGenderPreference(dto.roommatesGenderPreference());
        if (dto.hasWifi() != null) colocation.setHasWifi(dto.hasWifi());
        if (dto.hasParking() != null) colocation.setHasParking(dto.hasParking());
        if (dto.hasAirConditioning() != null) colocation.setHasAirConditioning(dto.hasAirConditioning());
        if (dto.isFurnished() != null) colocation.setIsFurnished(dto.isFurnished());
        if (dto.hasBalcony() != null) colocation.setHasBalcony(dto.hasBalcony());
        if (dto.hasPrivateBathroom() != null) colocation.setHasPrivateBathroom(dto.hasPrivateBathroom());
        if (dto.maxRoommates() != null) colocation.setMaxRoommates(dto.maxRoommates());
        if (dto.currentRoommates() != null) colocation.setCurrentRoommates(dto.currentRoommates());
        if (dto.status() != null) colocation.setStatus(dto.status());
        if (dto.rules() != null) colocation.setRules(dto.rules());
        if (dto.tags() != null) colocation.setTags(dto.tags());
        if (dto.imageUrls() != null) imageUpdate(dto, colocation);
        if (dto.averageRating() != null) colocation.setAverageRating(dto.averageRating());
        if (dto.reviews() != null)
            colocation.setReviews(dto.reviews().stream().map(ReviewMapper::toEntity).toList());
        if (dto.availableFrom() != null) colocation.setAvailableFrom(dto.availableFrom());
        if (dto.updatedAt() != null) colocation.setUpdatedAt(dto.updatedAt());
        if (dto.isArchived() != null) colocation.setIsArchived(dto.isArchived());
        if (dto.isPublished() != null) colocation.setIsPublished(dto.isPublished());

        return convertToDTO(colocationRepository.save(colocation));
    }
    public static void imageUpdate(UpdateColocationDTO colocationDTO, Colocation colocation) {
        if (colocationDTO.imageUrls() != null) {
            List<ColocationImage> images = colocationDTO.imageUrls().stream()
                    .map(url -> {
                        ColocationImage img = new ColocationImage();
                        img.setUrl(url);
                        img.setColocation(colocation); // lien bidirectionnel
                        return img;
                    })
                    .toList();
            colocation.setImages(images);
        }
    }
    public static void imageUpdate(ColocationDTO colocationDTO, Colocation colocation) {
        if (colocationDTO.imageUrls() != null) {
            List<ColocationImage> images = colocationDTO.imageUrls().stream()
                    .map(url -> {
                        ColocationImage img = new ColocationImage();
                        img.setUrl(url);
                        img.setColocation(colocation);
                        return img;
                    })
                    .toList();
            colocation.setImages(images);
        }
    }




    public static Review toEntity(ReviewDTO dto) {
        return ReviewMapper.toEntity(dto);
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
                colocation.getIsPublished(),
                colocation.getAssignedUserIds()
        );
    }

    public Colocation convertToEntity(CreateColocationDTO dto) {
        Colocation colocation = new Colocation();

        colocation.setName(dto.name());
        colocation.setAddress(dto.address());
        colocation.setCity(dto.city());
        colocation.setPostalCode(dto.postalCode());
        colocation.setDescription(dto.description());
        colocation.setPrice(dto.price());
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
        colocation.setAvailableFrom(dto.availableFrom());
        colocation.setCreatedAt(dto.createdAt());
        colocation.setUpdatedAt(dto.updatedAt());
        colocation.setIsArchived(dto.isArchived() != null ? dto.isArchived() : false);
        colocation.setIsPublished(dto.isPublished() != null ? dto.isPublished() : false);
        colocation.setAssignedUserIds(dto.assignedUserIds());

        // Les images et reviews doivent être gérées séparément
        // Exemple pour les images si tu veux les mapper :
    /*
    List<ColocationImage> imageEntities = dto.imageUrls().stream()
        .map(url -> {
            ColocationImage image = new ColocationImage();
            image.setUrl(url);
            image.setColocation(colocation); // nécessaire si tu veux les lier bidirectionnellement
            return image;
        })
        .collect(Collectors.toList());
    colocation.setImages(imageEntities);
    */

        return colocation;
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
        imageUpdate(dto, colocation);

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

    @Override
    public Colocation assignUserToColocation(Long colocationId, String userIdToAssign, String currentUserId, boolean isAdmin) {
        Colocation colocation = colocationRepository.findById(colocationId)
                .orElseThrow(() -> new RuntimeException("Colocation not found"));

        // Autorisation : admin OU propriétaire
        if (!isAdmin && !colocation.getIdOfPublisher().equals(currentUserId)) {
            throw new SecurityException("You are not authorized to assign users to this colocation.");
        }

        // Affectation
        colocation.assignUser(userIdToAssign);
        return colocationRepository.save(colocation);
    }

    @Override
    public Colocation removeAssignedUserFromColocation(Long colocationId, String userIdToRemove, String currentUserId, boolean isAdmin) {
        Colocation colocation = colocationRepository.findById(colocationId)
                .orElseThrow(() -> new RuntimeException("Colocation not found"));

        // Autorisation : admin OU propriétaire
        if (!isAdmin && !colocation.getIdOfPublisher().equals(currentUserId)) {
            throw new SecurityException("You are not authorized to remove users from this colocation.");
        }

        // Suppression
        colocation.removeAssignedUser(userIdToRemove);
        return colocationRepository.save(colocation);
    }

    @Override
    public Page<Colocation> getOwnColocations(String userId, String keyword, Pageable pageable) {
        Page<Colocation> colocations = colocationRepository.findOwnColocationsByKeyword(userId, keyword, pageable);

        // Récupérer tous les userIds assignés (évite les appels répétitifs)
        List<String> allAssignedUserIds = colocations.stream()
                .flatMap(coloc -> coloc.getAssignedUserIds().stream())
                .distinct()
                .toList();

        // Appel à userService pour récupérer les infos des utilisateurs
        List<Map<String, Object>> userInfos = userService.populateUsersById(allAssignedUserIds);

        // Indexation rapide par ID
        Map<String, Map<String, Object>> userInfoById = userInfos.stream()
                .collect(Collectors.toMap(user -> (String) user.get("id"), user -> user));

        // Injection des infos dans chaque colocation
        colocations.forEach(coloc -> {
            List<Map<String, Object>> infos = coloc.getAssignedUserIds().stream()
                    .map(userInfoById::get)
                    .filter(Objects::nonNull)
                    .toList();
            coloc.setAssignedUserInfos(infos);
        });

        return colocations;
    }





    public Page<ColocationDTO> getNonPublishedColocations(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Colocation> colocations;

        if (search == null || search.trim().isEmpty()) {
            colocations = colocationRepository.findNonPublishedColocations(pageable);
        } else {
            colocations = colocationRepository.searchNonPublished(search, pageable);
        }

        return colocations.map(this::convertToDTO);
    }

    @Override
    public Optional<Colocation> getColocationEntityById(Long id) {
        return colocationRepository.findById(id);
    }

    @Override
    public List<Long> getAssignedColocationIds(String userId) {
        return colocationRepository.findByAssignedUserIdsContaining(userId)
                .stream()
                .map(Colocation::getId)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ColocationDTO> getAssignedColocations(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return colocationRepository.findByAssignedUserIdsContaining(userId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Colocation toggleUserAssignment(Long colocationId, String userId, String currentUserId, boolean isAdmin) {
        Colocation colocation = colocationRepository.findById(colocationId)
                .orElseThrow(() -> new RuntimeException("Colocation with ID " + colocationId + " not found."));

        if (!isAdmin && !colocation.getIdOfPublisher().equals(currentUserId)) {
            throw new SecurityException("Only the owner or an admin can assign or unassign users.");
        }

        if (colocation.getAssignedUserIds().contains(userId)) {
            colocation.removeAssignedUser(userId);
        } else {
            colocation.assignUser(userId);
        }

        return colocationRepository.save(colocation);
    }

    @Override
    public ColocationDTO saveColocationWithImages(CreateColocationDTO dto, List<MultipartFile> images, String publisherId, String publisherUsername) {
        Colocation colocation = convertToEntity(dto);
        colocation.setIdOfPublisher(publisherId);
        colocation.setNameOfPublisher(publisherUsername);

        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                try {
                    Map uploadResult = cloudinary.uploader().upload(image.getBytes(), ObjectUtils.emptyMap());
                    String imageUrl = (String) uploadResult.get("secure_url");

                    ColocationImage colocationImage = new ColocationImage();
                    colocationImage.setUrl(imageUrl);
                    colocation.addImage(colocationImage);

                } catch (IOException e) {
                    throw new RuntimeException("Failed to upload image to Cloudinary", e);
                }
            }
        }

        Colocation saved = colocationRepository.save(colocation);
        return convertToDTO(saved);
    }


    private String saveImageToLocal(MultipartFile image) {
        try {
            String uploadDir = "uploads/images/colocations";
            Files.createDirectories(Paths.get(uploadDir));

            String originalFilename = image.getOriginalFilename();
            String fileName = UUID.randomUUID() + "_" + originalFilename;
            Path filePath = Paths.get(uploadDir, fileName);
            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }
    }


    @Override
    public ColocationDTO updateImages(Long colocationId, List<MultipartFile> newImages) {
        Colocation colocation = colocationRepository.findById(colocationId)
                .orElseThrow(() -> new ResourceNotFoundException("Colocation not found"));

        if (newImages != null && !newImages.isEmpty()) {
            for (MultipartFile image : newImages) {
                try {
                    Map uploadResult = cloudinary.uploader().upload(image.getBytes(), ObjectUtils.emptyMap());
                    String imageUrl = (String) uploadResult.get("secure_url");

                    ColocationImage imageEntity = new ColocationImage();
                    imageEntity.setUrl(imageUrl);
                    imageEntity.setColocation(colocation);
                    colocation.addImage(imageEntity);

                } catch (IOException e) {
                    throw new RuntimeException("Failed to upload image to Cloudinary", e);
                }
            }
        }

        Colocation updated = colocationRepository.save(colocation);
        return convertToDTO(updated);
    }


    @Override
    public void deleteImageByUrl(Long colocationId, String imageUrl) {
        Colocation colocation = colocationRepository.findById(colocationId)
                .orElseThrow(() -> new ResourceNotFoundException("Colocation not found"));

        ColocationImage imageToDelete = colocation.getImages().stream()
                .filter(img -> imageUrl.equals(img.getUrl()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Image with URL not found in this colocation"));

        colocation.getImages().remove(imageToDelete);
        colocationRepository.save(colocation);
    }

    public boolean isOwner(Long colocationId, String userId) {
        Colocation colocation = colocationRepository.findById(colocationId)
                .orElseThrow(() -> new ResourceNotFoundException("Colocation not found"));

        return colocation.getIdOfPublisher().equals(userId);
    }

}
