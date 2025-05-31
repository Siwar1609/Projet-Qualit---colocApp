package org.example.pfabackend.services.implementations;

import lombok.RequiredArgsConstructor;
import org.example.pfabackend.entities.Expense;
import org.example.pfabackend.entities.ExpenseShare;
import org.example.pfabackend.repositories.ExpenseRepository;
import org.example.pfabackend.repositories.ExpenseShareRepository;
import org.example.pfabackend.services.ExpenseShareService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExpenseShareServiceImpl implements ExpenseShareService {

    private final ExpenseShareRepository expenseShareRepository;
    private final ExpenseRepository expenseRepository;

    @Override
    public void deleteShare(Long shareId, String userId) {
        ExpenseShare share = expenseShareRepository.findByIdAndUserId(shareId, userId)
                .orElseThrow(() -> new RuntimeException("Not authorized or share not found"));

        expenseShareRepository.delete(share);

        // Recalcul automatique du datePaid de l'Expense
        Expense expense = share.getExpense();
        expenseRepository.save(expense); // Triggers @PreUpdate
    }

    @Override
    public void updatePaidStatus(Long shareId, String userId, boolean paid) {
        ExpenseShare share = expenseShareRepository.findByIdAndUserId(shareId, userId)
                .orElseThrow(() -> new RuntimeException("Not authorized or share not found"));

        share.setPaid(paid);
        expenseShareRepository.save(share);

        // Mettre à jour la dépense (datePaid auto)
        Expense expense = share.getExpense();
        expenseRepository.save(expense); // Triggers @PreUpdate
    }
}
