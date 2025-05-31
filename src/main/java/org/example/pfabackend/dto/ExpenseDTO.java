package org.example.pfabackend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ExpenseDTO {
    private Long id;
    private String label;
    private Double totalAmount;
    private LocalDate dateLimit;
    private String paidByUserId;
    private String paidByUserEmail;
    private Long colocationId;
    private List<ExpenseShareDTO> shares = new ArrayList<>();
    private LocalDate datePaid;

    public Boolean getIsPaid() {
        return datePaid != null;
    }

}
