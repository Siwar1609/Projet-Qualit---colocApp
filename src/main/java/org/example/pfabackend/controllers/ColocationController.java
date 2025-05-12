package org.example.pfabackend.controllers;

import jakarta.validation.Valid;
import org.example.pfabackend.dto.ErrorDTO;
import org.example.pfabackend.dto.ColocationDTO;
import org.example.pfabackend.services.ColocationService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
            @Valid @RequestBody ColocationDTO colocationDTO,
            @AuthenticationPrincipal Jwt jwt) {

        // Injecter l'idOfPublisher depuis le token JWT
        String userId = jwt.getClaimAsString("sub");
        String nameOfPublisher = jwt.getClaimAsString("preferred_username");

        // Update the DTO with the idOfPublisher and nameOfPublisher
        ColocationDTO updatedColocationDTO = new ColocationDTO(
                colocationDTO.id(),
                colocationDTO.name(),
                userId,
                nameOfPublisher,
                colocationDTO.address(),
                colocationDTO.city(),
                colocationDTO.postalCode(),
                colocationDTO.description(),
                colocationDTO.price(),
                colocationDTO.numberOfRooms(),
                colocationDTO.roommatesGenderPreference(),
                colocationDTO.hasWifi(),
                colocationDTO.hasParking(),
                colocationDTO.hasAirConditioning(),
                colocationDTO.isFurnished(),
                colocationDTO.hasBalcony(),
                colocationDTO.hasPrivateBathroom(),
                colocationDTO.maxRoommates(),
                colocationDTO.currentRoommates(),
                colocationDTO.status(),
                colocationDTO.rules(),
                colocationDTO.tags(),
                colocationDTO.imageUrls(),
                colocationDTO.averageRating(),
                colocationDTO.reviews(),
                colocationDTO.availableFrom(),
                colocationDTO.createdAt(),
                colocationDTO.updatedAt(),
                colocationDTO.isArchived(),
                colocationDTO.isPublished()
        );


        // Save the updated colocationDTO
        ColocationDTO created = colocationService.saveColocation(updatedColocationDTO);

        // Return the response
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createResponse("Colocation created successfully", created));
    }

    // PUT update colocation
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateColocation(@PathVariable Long id, @Valid @RequestBody ColocationDTO colocationDTO,@AuthenticationPrincipal Jwt jwt) {
        return colocationService.getColocationById(id,jwt)
                .map(existing -> {
                    ColocationDTO updated = colocationService.updateColocation(id, colocationDTO);
                    return ResponseEntity.ok(createResponse("Colocation updated successfully", updated));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Colocation with ID " + id + " not found")));
    }

    // DELETE colocation
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteColocation(@PathVariable Long id,@AuthenticationPrincipal Jwt jwt) {
        return colocationService.getColocationById(id,jwt)
                .map(existing -> {
                    colocationService.deleteColocation(id);
                    return ResponseEntity.ok(createResponse("Colocation with ID " + id + " has been deleted successfully", null));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Colocation with ID " + id + " not found")));
    }


    /**
     * Update the isPublished status of a Colocation
     */
    @PatchMapping("/{id}/publish")
    public ResponseEntity<Map<String, Object>> updateIsPublished(
            @PathVariable Long id, @RequestBody Map<String, Boolean> requestBody,@AuthenticationPrincipal Jwt jwt) {

        boolean isPublished = requestBody.getOrDefault("isPublished", false); // Default value is false
        ColocationDTO updatedColocation = colocationService.updateIsPublished(id, isPublished);

        return ResponseEntity.ok(createResponse("Colocation publication status updated successfully", updatedColocation));
    }

    /**
     * Update the isArchived status of a Colocation
     */
    @PatchMapping("/{id}/archive")
    public ResponseEntity<Map<String, Object>> updateIsArchived(
            @PathVariable Long id, @RequestBody Map<String, Boolean> requestBody) {

        boolean isArchived = requestBody.getOrDefault("isArchived", false); // Default value is false
        ColocationDTO updatedColocation = colocationService.updateIsArchived(id, isArchived);

        return ResponseEntity.ok(createResponse("Colocation archive status updated successfully", updatedColocation));
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
}
