package org.example.pfabackend.controller;

import jakarta.validation.Valid;
import org.example.pfabackend.model.ErrorDTO;
import org.example.pfabackend.model.ColocationDTO;
import org.example.pfabackend.service.ColocationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/colocations")
public class ColocationController {

    private final ColocationService colocationService;

    public ColocationController(ColocationService colocationService) {
        this.colocationService = colocationService;
    }

    @GetMapping
    public ResponseEntity<List<ColocationDTO>> getAllColocations() {
        List<ColocationDTO> colocations = colocationService.getAllColocations();
        return colocations.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(colocations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getColocationById(@PathVariable Long id) {
        return colocationService.getColocationById(id)
                .map(existingColocation -> ResponseEntity.ok(createResponse("Colocation found successfully", existingColocation)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Colocation with ID " + id + " not found")));
    }


    @PostMapping
    public ResponseEntity<?> createColocation(@Valid @RequestBody ColocationDTO colocationDTO) {
        ColocationDTO createdColocation = colocationService.saveColocation(colocationDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createResponse("Colocation created successfully", createdColocation));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateColocation(@PathVariable Long id, @Valid @RequestBody ColocationDTO colocationDTO) {
        return colocationService.getColocationById(id)
                .map(existingColocation -> {
                    ColocationDTO updatedColocation = colocationService.updateColocation(id, colocationDTO);
                    return ResponseEntity.ok(createResponse("Colocation updated successfully", updatedColocation));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Colocation with ID " + id + " not found")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteColocation(@PathVariable Long id) {
        return colocationService.getColocationById(id)
                .map(existingColocation -> {
                    colocationService.deleteColocation(id);
                    return ResponseEntity.ok(createResponse("Colocation with ID " + id + " has been deleted successfully", null));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Colocation with ID " + id + " not found")));
    }

    // Helper method to create a standardized success response
    private Map<String, Object> createResponse(String message, Object data) {
        return Map.of("message", message, "data", data != null ? data : "No data available");
    }

    // Helper method to create a standardized error response
    private Map<String, Object> createErrorResponse(String errorMessage) {
        return Map.of("error", new ErrorDTO(errorMessage));
    }

    // Global exception handler (optional) to catch specific exceptions and centralize error responses.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorDTO("An unexpected error occurred: " + e.getMessage()));
    }
}
