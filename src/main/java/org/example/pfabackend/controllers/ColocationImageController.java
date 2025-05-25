package org.example.pfabackend.controllers;

import lombok.RequiredArgsConstructor;
import org.example.pfabackend.dto.ColocationImageDTO;
import org.example.pfabackend.entities.Colocation;
import org.example.pfabackend.services.ColocationImageService;
import org.example.pfabackend.services.ColocationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/colocations/{colocationId}/images")
@RequiredArgsConstructor
public class ColocationImageController {

    private final ColocationImageService colocationImageService;
    private final ColocationService colocationService;

    @GetMapping
    public ResponseEntity<List<ColocationImageDTO>> getImages(@PathVariable Long colocationId) {
        List<ColocationImageDTO> images = colocationImageService.getImagesByColocationId(colocationId);
        return ResponseEntity.ok(images);
    }

    @PostMapping
    public ResponseEntity<ColocationImageDTO> addImage(
            @PathVariable Long colocationId,
            @RequestBody ColocationImageDTO imageDTO,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getClaimAsString("sub");
        List<String> roles = jwt.getClaimAsStringList("roles");

        // Récupérer l'entité Colocation (non DTO) via une méthode dédiée dans le service
        Optional<Colocation> optColocation = colocationService.getColocationEntityById(colocationId);
        if (optColocation.isEmpty()) {
            // Colocation non trouvée
            return ResponseEntity.notFound().build();
        }
        Colocation colocation = optColocation.get();

        // Vérifier que l'utilisateur est propriétaire ou admin
        if (!isOwnerOrAdmin(colocation, userId, roles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Ajouter l'image via le service dédié
        ColocationImageDTO savedImage = colocationImageService.addImage(colocationId, imageDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedImage);
    }



    @PutMapping("/{imageId}")
    public ResponseEntity<ColocationImageDTO> updateImage(
            @PathVariable Long colocationId,
            @PathVariable Long imageId,
            @RequestBody ColocationImageDTO imageDTO,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getClaimAsString("sub");
        List<String> roles = jwt.getClaimAsStringList("roles");

        Optional<Colocation> optColocation = colocationService.getColocationEntityById(colocationId);
        if (optColocation.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Colocation colocation = optColocation.get();

        if (!isOwnerOrAdmin(colocation, userId, roles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ColocationImageDTO updatedImage = colocationImageService.updateImage(colocationId, imageId, imageDTO);
        if (updatedImage == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedImage);
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long colocationId,
            @PathVariable Long imageId,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getClaimAsString("sub");
        List<String> roles = jwt.getClaimAsStringList("roles");

        Optional<Colocation> optColocation = colocationService.getColocationEntityById(colocationId);
        if (optColocation.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Colocation colocation = optColocation.get();

        if (!isOwnerOrAdmin(colocation, userId, roles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean deleted = colocationImageService.deleteImage(colocationId, imageId);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }


    private boolean isOwnerOrAdmin(Colocation colocation, String userId, List<String> roles) {
        boolean isOwner = userId.equals(colocation.getIdOfPublisher());
        boolean isAdmin = roles != null && roles.contains("ADMIN");
        return isOwner || isAdmin;
    }

}
