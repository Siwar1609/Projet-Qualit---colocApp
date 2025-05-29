package org.example.pfabackend.repositories;

import org.example.pfabackend.entities.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByColocationId(Long colocationId);
    List<Expense> findByPaidByUserEmail(String userEmail);
    @Query("SELECT DISTINCT e FROM Expense e JOIN e.shares s WHERE s.paid = false")
    List<Expense> findBySharesPaidFalse();

}
