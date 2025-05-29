package org.example.pfabackend.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ExpenseShareDTO {
    private String userId;
    private String userEmail;
    private Double amount;
    private Boolean paid;
    private LocalDate datePaid;


}
