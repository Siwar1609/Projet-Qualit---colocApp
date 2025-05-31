package org.example.pfabackend.services;

import org.example.pfabackend.dto.*;
import org.example.pfabackend.entities.Expense;

import java.util.List;
import java.util.Map;

public interface ExpenseService {
    ExpenseDTO createExpense(ExpenseDTO dto);
    List<ExpenseDTO> getExpensesForColocation(Long colocationId);
    Map<String, Double> calculateBalances(Long colocationId);
    ExpenseDTO updateExpense(Long expenseId, ExpenseDTO dto);
    ExpenseDTO updateExpenseShares(Long expenseId, List<ExpenseShareDTO> shares);
    UserExpenseSummaryDTO getUserExpensesFiltered(String userId, boolean paid);

    ExpenseDTO getExpenseById(Long id);
    List<ExpenseWithStatsDTO> getExpensesWithStatsByUserEmail(String userEmail);
    List<UserColocationStatsDTO> getStatisticsByUserEmail(String userEmail);

    List<Expense> getExpenses(Long colocationId, String userId);

    void deleteExpense(Long id, String userId);
    List<ExpenseDTO> getAllExpenses();
    List<ExpenseDTO> getExpensesForUser(String userId, boolean share);
}

