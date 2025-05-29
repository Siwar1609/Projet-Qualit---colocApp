package org.example.pfabackend.dto;

import lombok.Data;
import java.util.Map;

@Data
public class UserColocationStatsDTO {
    private Long colocationId;
    private double totalSpent; // total payé par l'utilisateur dans cette coloc
    private double totalOwed;  // total dû par l'utilisateur dans cette coloc
    private Map<String, Double> typeWiseAmount; // ex : {"FOOD": 100.0, "RENT": 300.0}
}
