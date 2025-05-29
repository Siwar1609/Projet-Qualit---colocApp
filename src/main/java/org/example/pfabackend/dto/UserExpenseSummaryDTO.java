package org.example.pfabackend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserExpenseSummaryDTO {
    private List<ExpenseDTO> expenses;
    private Double totalUnpaidAmount;

    // getters/setters, constructor
}
