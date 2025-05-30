package org.example.pfabackend.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
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

   // @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(name = "available_from")
    private LocalDate availableFrom;

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
    @JsonManagedReference
    private List<ColocationImage> images = new ArrayList<>();

    //@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
   // @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDate updatedAt;

    @OneToMany(mappedBy = "colocation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;


    @Column(name = "is_archived", nullable = false)
    private Boolean isArchived = false;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = false;

    @Transient
    private Double averageRating; // optional transient field for displaying average

    //@ElementCollection
    //@CollectionTable(name = "colocation_images", joinColumns = @JoinColumn(name = "colocation_id"))
    //@Column(name = "image_url")
    //private List<String> imageUrls;

    @ElementCollection
    @CollectionTable(name = "colocation_assigned_users", joinColumns = @JoinColumn(name = "colocation_id"))
    @Column(name = "user_id")
    private List<String> assignedUserIds = new ArrayList<>();


    public boolean assignUser(String userId) {
        if (assignedUserIds.contains(userId)) {
            throw new IllegalArgumentException("User is already assigned to this colocation.");
        }
        if (assignedUserIds.size() >= maxRoommates) {
            throw new IllegalStateException("Max number of roommates reached.");
        }

        assignedUserIds.add(userId);
        this.currentRoommates = assignedUserIds.size();
        return true;
    }

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
    public void addImage(ColocationImage image) {
        images.add(image);
        image.setColocation(this);
    }

    public void removeImage(ColocationImage image) {
        images.remove(image);
        image.setColocation(null);
    }

    @PrePersist
    @PreUpdate
    private void validate() {
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Price must be >= 0");
        }
    }

}
