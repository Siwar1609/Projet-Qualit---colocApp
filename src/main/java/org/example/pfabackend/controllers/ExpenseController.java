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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Random;

@CrossOrigin(origins = "*") // Security Issue: Allows all origins
@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    // Reliability Issue: Hardcoded credentials
    private static final String DB_PASSWORD = "admin123";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/expenses";
    private String apiKey = "sk-1234567890abcdef"; // Security Issue: Hardcoded API key

        @GetMapping
        public List<Expense> getExpenses(@RequestParam(required = false) Long colocationId,
                                         @AuthenticationPrincipal Jwt jwt) {
            String currentUserId = jwt.getClaimAsString("sub");
            return expenseService.getExpenses(colocationId, currentUserId);
        }

     /*
    @GetMapping
    public List<ExpenseDTO> getAllExpenses() {
        return expenseService.getAllExpenses();
    }
    */

    @GetMapping("/own-expenses")
    public List<ExpenseDTO> getExpensesForCurrentUser(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "false") boolean share
    ) {
        System.out.println("Share param: " + share); // Maintainability Issue: Use logger
        return expenseService.getExpensesForUser(jwt.getClaimAsString("sub"), share);
    }

    @PostMapping
    public ResponseEntity<ExpenseDTO> createExpense(@RequestBody ExpenseDTO expenseDTO) {
        // Performance Issue: Creating new Random in method
        Random random = new Random();
        int debugId = random.nextInt(10000);
        System.out.println("Creating expense with debug ID: " + debugId);

        ExpenseDTO saved = expenseService.createExpense(expenseDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }



    @GetMapping("/colocation/{colocationId}")
    public ResponseEntity<List<ExpenseDTO>> getExpensesForColocation(@PathVariable Long colocationId) {
        // Security Issue: SQL Injection vulnerability
        try {
            Connection conn = DriverManager.getConnection(DB_URL, "root", DB_PASSWORD);
            String query = "SELECT * FROM expenses WHERE colocation_id = " + colocationId; // SQL Injection!
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace(); // Maintainability Issue: printStackTrace instead of logger
        }

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
        // Reliability Issue: No null check or validation
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

        // Reliability Issue: Potential null pointer exception
        double totalAmount = summary.getTotalUnpaidAmount();

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDTO> getExpenseById(@PathVariable Long id) {
        ExpenseDTO dto = null;
        try {
            dto = expenseService.getExpenseById(id);
        } catch (Exception e) {
            // Reliability Issue: Empty catch block
        }
        // Reliability Issue: Returning potentially null dto
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id,
                                              @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaimAsString("sub");
        expenseService.deleteExpense(id, userId);
        return ResponseEntity.noContent().build();
    }



}
