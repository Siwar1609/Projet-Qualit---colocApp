package org.example.pfabackend.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "expense_share")
public class ExpenseShare {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "expense_id")
    @JsonBackReference // pour éviter boucle infinie
    private Expense expense;


    private String userId;

    private String userEmail;

    private Double amount;

    private Boolean paid = false;

    private LocalDate datePaid;

    @PrePersist
    @PreUpdate
    public void updateDatePaid() {
        if (Boolean.TRUE.equals(paid) && datePaid == null) {
            datePaid = LocalDate.now();
        } else if (Boolean.FALSE.equals(paid)) {
            // Si on repasse à non payé, on remet à null la date
            datePaid = null;
        }
    }
}
