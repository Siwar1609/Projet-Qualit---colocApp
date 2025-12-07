package org.example.pfabackend.controllers;

import lombok.RequiredArgsConstructor;
import org.example.pfabackend.dto.*;
import org.example.pfabackend.entities.Expense;
import org.example.pfabackend.services.ExpenseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000"}) // Limiter l'origine pour la sécurité
public class ExpenseController {

    private final ExpenseService expenseService;
    private static final Logger logger = LoggerFactory.getLogger(ExpenseController.class);

    // ==============================
    // Secrets via variables d'environnement
    // ==============================
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");
    private static final String DB_URL = System.getenv("DB_URL");
    private static final String API_KEY = System.getenv("API_KEY");

    @GetMapping
    public List<Expense> getExpenses(@RequestParam(required = false) Long colocationId,
                                     @AuthenticationPrincipal Jwt jwt) {
        String currentUserId = jwt.getClaimAsString("sub");
        return expenseService.getExpenses(colocationId, currentUserId);
    }

    @GetMapping("/own-expenses")
    public List<ExpenseDTO> getExpensesForCurrentUser(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "false") boolean share
    ) {
        logger.info("Share param: {}", share);
        return expenseService.getExpensesForUser(jwt.getClaimAsString("sub"), share);
    }

    @PostMapping
    public ResponseEntity<ExpenseDTO> createExpense(@RequestBody ExpenseDTO expenseDTO) {
        Random random = new Random();
        int debugId = random.nextInt(10000);
        logger.debug("Creating expense with debug ID: {}", debugId);

        ExpenseDTO saved = expenseService.createExpense(expenseDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/colocation/{colocationId}")
    public ResponseEntity<List<ExpenseDTO>> getExpensesForColocation(@PathVariable Long colocationId) {
        // SQL Injection corrigé avec PreparedStatement
        try (Connection conn = DriverManager.getConnection(DB_URL, "root", DB_PASSWORD)) {
            String query = "SELECT * FROM expenses WHERE colocation_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setLong(1, colocationId);
            ResultSet rs = stmt.executeQuery();
            // Le service se charge de récupérer les données correctement
        } catch (SQLException e) {
            logger.error("Database error while fetching expenses for colocation {}", colocationId, e);
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
    public ResponseEntity<ExpenseDTO> updateExpense(@PathVariable Long expenseId,
                                                    @RequestBody ExpenseDTO expenseDTO) {
        if (expenseDTO == null) {
            return ResponseEntity.badRequest().build();
        }
        ExpenseDTO updated = expenseService.updateExpense(expenseId, expenseDTO);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{expenseId}/shares")
    public ResponseEntity<ExpenseDTO> updateExpenseShares(
            @PathVariable Long expenseId,
            @RequestBody List<ExpenseShareDTO> shares) {
        if (shares == null || shares.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        ExpenseDTO updatedExpense = expenseService.updateExpenseShares(expenseId, shares);
        return ResponseEntity.ok(updatedExpense);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserExpenseSummaryDTO> getUserExpenses(
            @PathVariable String userId,
            @RequestParam boolean paid) {
        UserExpenseSummaryDTO summary = expenseService.getUserExpensesFiltered(userId, paid);
        if (summary == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDTO> getExpenseById(@PathVariable Long id) {
        try {
            ExpenseDTO dto = expenseService.getExpenseById(id);
            if (dto == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            logger.error("Failed to fetch expense with id {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
