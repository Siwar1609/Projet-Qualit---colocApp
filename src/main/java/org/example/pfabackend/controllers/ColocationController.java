package org.example.pfabackend.controllers;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.example.pfabackend.dto.CreateColocationDTO;
import org.example.pfabackend.dto.ErrorDTO;
import org.example.pfabackend.dto.ColocationDTO;
import org.example.pfabackend.dto.UpdateColocationDTO;
import org.example.pfabackend.entities.Colocation;
import org.example.pfabackend.services.ColocationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Validated
@RestController
@RequestMapping("/api/colocations")
public class ColocationController {

    private final ColocationService colocationService;

    public ColocationController(ColocationService colocationService) {
        this.colocationService = colocationService;
    }

    // GET paginated + searchable colocations
    @GetMapping
    public ResponseEntity<Page<ColocationDTO>> getAllColocations(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Jwt jwt) { // Inject Jwt token here

        // Pass the Jwt token to the service to check the user's role
        Page<ColocationDTO> result = colocationService.getAllColocations(search, page, size, jwt);
        return ResponseEntity.ok(result);
    }

    // GET colocation by ID
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getColocationById(@PathVariable Long id  ,@AuthenticationPrincipal Jwt jwt) {
        return colocationService.getColocationById(id,jwt)
                .map(colocation -> ResponseEntity.ok(createResponse("Colocation found successfully", colocation)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Colocation with ID " + id + " not found")));
    }

    // POST create colocation
    @PostMapping
    public ResponseEntity<Map<String, Object>> createColocation(
            @Valid @RequestBody CreateColocationDTO dto,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getClaimAsString("sub");
        String nameOfPublisher = jwt.getClaimAsString("preferred_username");

        ColocationDTO fullDTO = new ColocationDTO(
                null, // id sera généré
                dto.name(),
                userId,
                nameOfPublisher,
                dto.address(),
                dto.city(),
                dto.postalCode(),
                dto.description(),
                dto.price(),
                dto.numberOfRooms(),
                dto.roommatesGenderPreference(),
                dto.hasWifi(),
                dto.hasParking(),
                dto.hasAirConditioning(),
                dto.isFurnished(),
                dto.hasBalcony(),
                dto.hasPrivateBathroom(),
                dto.maxRoommates(),
                dto.currentRoommates(),
                dto.status(),
                dto.rules(),
                dto.tags(),
                dto.imageUrls(),
                dto.averageRating(),
                null,
                dto.availableFrom(),
                dto.createdAt(),
                dto.updatedAt(),
                dto.isArchived(),
                dto.isPublished()
        );

        ColocationDTO created = colocationService.saveColocation(fullDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createResponse("Colocation created successfully", created));
    }


    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateColocation(
            @PathVariable Long id,
            @RequestBody UpdateColocationDTO updateDTO,
            @AuthenticationPrincipal Jwt jwt) {

        return colocationService.getColocationById(id, jwt)
                .map(existing -> {
                    ColocationDTO updated = colocationService.updateColocation(id, updateDTO);
                    return ResponseEntity.ok(createResponse("Colocation updated successfully", updated));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Colocation with ID " + id + " not found")));
    }


    // DELETE colocation
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteColocation(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Recherche de la colocation à supprimer
            Optional<ColocationDTO> existingColocation = colocationService.getColocationById(id, jwt);

            if (existingColocation.isPresent()) {
                // Supprimer la colocation si elle existe
                colocationService.deleteColocation(id);

                response.put("status", HttpStatus.OK.value());
                response.put("message", "Colocation with ID " + id + " has been deleted successfully.");
                return ResponseEntity.ok(response);
            } else {
                // Si la colocation n'existe pas
                response.put("status", HttpStatus.NOT_FOUND.value());
                response.put("error", "Not Found");
                response.put("message", "Colocation with ID " + id + " not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception ex) {
            // Gestion d'erreur inattendue
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("error", "Internal Server Error");
            response.put("message", "An unexpected error occurred: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Update the isPublished status of a Colocation
     */
    @PatchMapping("/{id}/publish")
    public ResponseEntity<Map<String, Object>> updateIsPublished(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> requestBody,
            @AuthenticationPrincipal Jwt jwt) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (!requestBody.containsKey("isPublished")) {
                response.put("status", HttpStatus.BAD_REQUEST.value());
                response.put("error", "Bad Request");
                response.put("message", "Missing required field 'isPublished' in request body.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            boolean isPublished = requestBody.get("isPublished");

            ColocationDTO updatedColocation = colocationService.updateIsPublished(id, isPublished);

            response.put("status", HttpStatus.OK.value());
            response.put("message", "Colocation publication status updated successfully.");
            response.put("data", updatedColocation);
            return ResponseEntity.ok(response);

        } catch (EntityNotFoundException ex) {
            response.put("status", HttpStatus.NOT_FOUND.value());
            response.put("error", "Not Found");
            response.put("message", "Colocation with id " + id + " not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (IllegalArgumentException ex) {
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("error", "Invalid Request");
            response.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception ex) {
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("error", "Internal Server Error");
            response.put("message", "An unexpected error occurred: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @PatchMapping("/{id}/archive")
    public ResponseEntity<Map<String, Object>> updateIsArchived(
            @PathVariable Long id, @RequestBody Map<String, Boolean> requestBody) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Vérifier si le champ "isArchived" est présent dans la requête
            if (!requestBody.containsKey("isArchived")) {
                response.put("status", HttpStatus.BAD_REQUEST.value());
                response.put("error", "Bad Request");
                response.put("message", "Missing required field 'isArchived' in request body.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Extraire la valeur du champ "isArchived" ou utiliser la valeur par défaut (false)
            boolean isArchived = requestBody.get("isArchived");

            // Appel du service pour mettre à jour l'état de l'archive
            ColocationDTO updatedColocation = colocationService.updateIsArchived(id, isArchived);

            // Réponse de succès
            response.put("status", HttpStatus.OK.value());
            response.put("message", "Colocation archive status updated successfully.");
            response.put("data", updatedColocation);
            return ResponseEntity.ok(response);

        } catch (EntityNotFoundException ex) {
            // Cas où la colocation n'a pas été trouvée
            response.put("status", HttpStatus.NOT_FOUND.value());
            response.put("error", "Not Found");
            response.put("message", "Colocation with id " + id + " not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (IllegalArgumentException ex) {
            // Cas où une erreur d'argument invalide se produit
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("error", "Invalid Request");
            response.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception ex) {
            // Cas où une exception non gérée se produit
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("error", "Internal Server Error");
            response.put("message", "An unexpected error occurred: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Helper: success response
    private Map<String, Object> createResponse(String message, Object data) {
        return Map.of("message", message, "data", data != null ? data : "No data available");
    }

    // Helper: error response
    private Map<String, Object> createErrorResponse(String errorMessage) {
        return Map.of("error", new ErrorDTO(errorMessage));
    }

    // Global exception handler (fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorDTO("An unexpected error occurred: " + e.getMessage()));
    }

    // In ColocationController.java

    // Get non-published colocations for admin
    @GetMapping("/non-published")
    public ResponseEntity<Page<ColocationDTO>> getNonPublishedColocations(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Jwt jwt) {


        // Retrieve non-published colocations
        Page<ColocationDTO> result = colocationService.getNonPublishedColocations(search,page, size);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/assign/{userIdToAssign}")
    public ResponseEntity<?> assignUserToColocation(
            @PathVariable Long id,
            @PathVariable String userIdToAssign,
            @AuthenticationPrincipal Jwt jwt) {

        String currentUserId = jwt.getClaimAsString("sub");
        List<String> roles = jwt.getClaims().containsKey("roles")
                ? jwt.getClaimAsStringList("roles")
                : new ArrayList<>();

        boolean isAdmin = roles.contains("ADMIN");

        try {
            Colocation updated = colocationService.assignUserToColocation(id, userIdToAssign, currentUserId, isAdmin);
            return ResponseEntity.ok(updated);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-colocations")
    public ResponseEntity<Page<Colocation>> getMyColocations(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {

        String currentUserId = jwt.getClaimAsString("sub");
        Pageable pageable = PageRequest.of(page, size);
        Page<Colocation> colocations = colocationService.getOwnColocations(currentUserId, keyword, pageable);

        return ResponseEntity.ok(colocations);
    }




}
