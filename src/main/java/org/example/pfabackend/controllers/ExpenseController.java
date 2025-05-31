package org.example.pfabackend.controllers;

import lombok.RequiredArgsConstructor;
import org.example.pfabackend.dto.*;
import org.example.pfabackend.entities.Expense;
import org.example.pfabackend.services.ExpenseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseDTO> createExpense(@RequestBody ExpenseDTO expenseDTO) {
        ExpenseDTO saved = expenseService.createExpense(expenseDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }



    @GetMapping("/colocation/{colocationId}")
    public ResponseEntity<List<ExpenseDTO>> getExpensesForColocation(@PathVariable Long colocationId) {
        List<ExpenseDTO> expenses = expenseService.getExpensesForColocation(colocationId);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/colocation/{colocationId}/balance")
    public ResponseEntity<Map<String, Double>> getBalancePerUser(@PathVariable Long colocationId) {
        Map<String, Double> balance = expenseService.calculateBalances(colocationId);
        return ResponseEntity.ok(balance);
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<ExpenseDTO> updateExpense(@PathVariable Long expenseId, @RequestBody ExpenseDTO expenseDTO) {
        ExpenseDTO updated = expenseService.updateExpense(expenseId, expenseDTO);
        return ResponseEntity.ok(updated);
    }
    @PatchMapping("/{expenseId}/shares")
    public ResponseEntity<ExpenseDTO> updateExpenseShares(
            @PathVariable Long expenseId,
            @RequestBody List<ExpenseShareDTO> shares) {
        ExpenseDTO updatedExpense = expenseService.updateExpenseShares(expenseId, shares);
        return ResponseEntity.ok(updatedExpense);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserExpenseSummaryDTO> getUserExpenses(
            @PathVariable String userId,
            @RequestParam boolean paid) {
        UserExpenseSummaryDTO summary = expenseService.getUserExpensesFiltered(userId, paid);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDTO> getExpenseById(@PathVariable Long id) {
        ExpenseDTO dto = expenseService.getExpenseById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/byUserEmail")
    public ResponseEntity<List<ExpenseWithStatsDTO>> getExpensesByUserEmail(@RequestParam String userEmail) {
        List<ExpenseWithStatsDTO> dtos = expenseService.getExpensesWithStatsByUserEmail(userEmail);
        return ResponseEntity.ok(dtos);
    }
    @GetMapping("/stats")
    public ResponseEntity<List<UserColocationStatsDTO>> getStats(@RequestParam String userEmail) {
        return ResponseEntity.ok(expenseService.getStatisticsByUserEmail(userEmail));
    }

    @GetMapping
    public List<Expense> getExpenses(@RequestParam(required = false) Long colocationId,
                                     @AuthenticationPrincipal Jwt jwt) {
        String currentUserId = jwt.getClaimAsString("sub");
        return expenseService.getExpenses(colocationId, currentUserId);
    }

}
