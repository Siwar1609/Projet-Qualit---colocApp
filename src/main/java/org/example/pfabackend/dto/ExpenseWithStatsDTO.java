package org.example.pfabackend.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class ExpenseWithStatsDTO {
    private Long id;
    private String label;
    private Double totalAmount;
    private LocalDate dateLimit;
    private LocalDate datePaid;
    private String paidByUserEmail;
    private Long colocationId;

    private Double totalPaidShares;
    private Double totalUnpaidShares;

    // Getters & setters
}
