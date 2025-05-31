package org.example.pfabackend.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "expense")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String label; // e.g. "Electricity Bill", "Groceries"

    private Double totalAmount;

    private LocalDate dateLimit;

    private LocalDate datePaid;


    @ManyToOne
    private Colocation colocation;

    private String paidByUserId;
    private String paidByUserEmail;

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ExpenseShare> shares = new ArrayList<>();


    @PrePersist
    @PreUpdate
    public void updateDatePaidIfAllSharesPaid() {
        if (areAllSharesPaid()) {
            if (datePaid == null) {
                datePaid = LocalDate.now();
            }
        } else {
            // Si pas tous payés, on annule la date
            datePaid = null;
        }
    }

    public boolean areAllSharesPaid() {
        return shares != null && !shares.isEmpty() && shares.stream().allMatch(ExpenseShare::getPaid);
    }
}
