package org.example.pfabackend.repository;
import org.example.pfabackend.model.Colocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ColocationRepository extends JpaRepository<Colocation, Long> {
}