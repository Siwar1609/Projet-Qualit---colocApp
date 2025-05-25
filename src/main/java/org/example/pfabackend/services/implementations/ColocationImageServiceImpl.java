package org.example.pfabackend.services.implementations;


import lombok.RequiredArgsConstructor;
import org.example.pfabackend.dto.ColocationImageDTO;
import org.example.pfabackend.entities.Colocation;
import org.example.pfabackend.entities.ColocationImage;
import org.example.pfabackend.repositories.ColocationImageRepository;
import org.example.pfabackend.repositories.ColocationRepository;
import org.example.pfabackend.services.ColocationImageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ColocationImageServiceImpl implements ColocationImageService {

    private final ColocationRepository colocationRepository;
    private final ColocationImageRepository imageRepository;

    @Override
    public List<ColocationImageDTO> getImagesByColocationId(Long colocationId) {
        return imageRepository.findByColocationId(colocationId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ColocationImageDTO addImage(Long colocationId, ColocationImageDTO imageDTO) {
        Colocation colocation = colocationRepository.findById(colocationId)
                .orElseThrow(() -> new IllegalArgumentException("Colocation not found"));

        ColocationImage image = new ColocationImage();
        image.setUrl(imageDTO.getUrl());
        image.setColocation(colocation);

        colocation.addImage(image);
        colocationRepository.save(colocation); // save parent to persist cascade

        return toDTO(image);
    }

    @Override
    public ColocationImageDTO updateImage(Long colocationId, Long imageId, ColocationImageDTO imageDTO) {
        ColocationImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found"));

        if (!image.getColocation().getId().equals(colocationId)) {
            throw new IllegalArgumentException("Image does not belong to the specified colocation");
        }

        image.setUrl(imageDTO.getUrl());
        return toDTO(image);
    }

    @Override
    public boolean deleteImage(Long colocationId, Long imageId) {
        Optional<ColocationImage> optImage = imageRepository.findById(imageId);
        if (optImage.isEmpty()) {
            return false; // Image non trouvée
        }

        ColocationImage image = optImage.get();

        if (!image.getColocation().getId().equals(colocationId)) {
            return false; // L'image ne correspond pas à la colocation
        }

        Colocation colocation = image.getColocation();
        colocation.removeImage(image);
        colocationRepository.save(colocation);

        // Supprimer aussi l'image de la base (si tu souhaites vraiment la supprimer)
        imageRepository.delete(image);

        return true; // Suppression réussie
    }


    private ColocationImageDTO toDTO(ColocationImage image) {
        ColocationImageDTO dto = new ColocationImageDTO();
        dto.setId(image.getId());
        dto.setUrl(image.getUrl());
        return dto;
    }
}
