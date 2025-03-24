package org.example.pfabackend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ColocationDTO(
        Long id,

        @NotNull(message = "Name cannot be null")
        @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
        String name,

        @NotNull(message = "ID of Publisher cannot be null")
        String idOfPublisher,

        @NotNull(message = "Name of Publisher cannot be null")
        String nameOfPublisher,

        @NotNull(message = "Address cannot be null")
        String address,

        String description, // Optional field

        @NotNull(message = "Price cannot be null")
        double price
) {
    // Custom constructor with validation
    public ColocationDTO {
        // You can add additional validation logic here if needed
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
    }
}