package org.example.pfabackend.controllers;

import lombok.RequiredArgsConstructor;
import org.example.pfabackend.services.ExpenseShareService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shares")
@RequiredArgsConstructor
public class ExpenseShareController {

    private final ExpenseShareService expenseShareService;

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShare(@PathVariable Long id,
                                            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaimAsString("sub");
        expenseShareService.deleteShare(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/paid")
    public ResponseEntity<Void> markAsPaid(@PathVariable Long id,
                                           @RequestParam boolean paid,
                                           @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaimAsString("sub");
        expenseShareService.updatePaidStatus(id, userId, paid);
        return ResponseEntity.ok().build();
    }
}
