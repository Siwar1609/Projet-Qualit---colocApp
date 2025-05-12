package org.example.pfabackend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "colocation")
@Getter
@Setter
public class Colocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "id_of_publisher", nullable = false)
    private String idOfPublisher;

    @Column(name = "name_of_publisher", nullable = false)
    private String nameOfPublisher;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "available_from")
    private LocalDateTime availableFrom;

    @Column(name = "number_of_rooms", nullable = false)
    private Integer numberOfRooms=1;

    @Column(name = "roommates_gender_preference")
    private String roommatesGenderPreference; // e.g., "Male", "Female", "Mixed"

    @Column(name = "has_wifi")
    private Boolean hasWifi;

    @Column(name = "has_parking")
    private Boolean hasParking;

    @Column(name = "has_air_conditioning")
    private Boolean hasAirConditioning;

    @Column(name = "is_furnished")
    private Boolean isFurnished;

    @Column(name = "has_balcony")
    private Boolean hasBalcony;

    @Column(name = "has_private_bathroom")
    private Boolean hasPrivateBathroom;

    @Column(name = "max_roommates")
    private Integer maxRoommates;

    @Column(name = "current_roommates")
    private Integer currentRoommates;

    @Column(name = "status")
    private String status; // e.g., "Available", "Occupied", "Pending"

    @ElementCollection
    @CollectionTable(name = "colocation_rules", joinColumns = @JoinColumn(name = "colocation_id"))
    @Column(name = "rule")
    private List<String> rules; // e.g., "No smoking", "No pets", "Vegetarian only"

    @ElementCollection
    @CollectionTable(name = "colocation_tags", joinColumns = @JoinColumn(name = "colocation_id"))
    @Column(name = "tag")
    private List<String> tags; // e.g., "Near university", "Quiet area"

    @OneToMany(mappedBy = "colocation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ColocationImage> images = new ArrayList<>();;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @OneToMany(mappedBy = "colocation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;


    @Column(name = "is_archived", nullable = false)
    private Boolean isArchived = false;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = false;

    @Transient
    private Double averageRating; // optional transient field for displaying average

    @PostLoad
    public void calculateAverageRating() {
        if (reviews != null && !reviews.isEmpty()) {
            this.averageRating = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
        } else {
            this.averageRating = 0.0;
        }
    }

}
