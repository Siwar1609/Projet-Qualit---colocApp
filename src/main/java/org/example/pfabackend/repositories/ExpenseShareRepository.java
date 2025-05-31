package org.example.pfabackend.repositories;

import org.example.pfabackend.entities.ExpenseShare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExpenseShareRepository extends JpaRepository<ExpenseShare, Long> {
    Optional<ExpenseShare> findByIdAndUserId(Long id, String userId);
}
