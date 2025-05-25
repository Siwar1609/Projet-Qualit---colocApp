package org.example.pfabackend.repositories;

import org.example.pfabackend.entities.Colocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ColocationRepository extends JpaRepository<Colocation, Long> {

    @Query("SELECT c FROM Colocation c LEFT JOIN c.rules r LEFT JOIN c.tags t WHERE " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.nameOfPublisher) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.address) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.city) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.postalCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.status) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.roommatesGenderPreference) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Colocation> search(@Param("keyword") String keyword, Pageable pageable);
    @Query("SELECT c FROM Colocation c LEFT JOIN c.rules r LEFT JOIN c.tags t WHERE " +
            "(LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.nameOfPublisher) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.address) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.city) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.postalCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.status) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.roommatesGenderPreference) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "c.isPublished = true AND c.isArchived = false")
    Page<Colocation> searchPublic(@Param("keyword") String keyword, Pageable pageable);
    Page<Colocation> findByIsPublishedTrueAndIsArchivedFalse(Pageable pageable);
    Optional<Colocation> findByIdAndIsPublishedTrueAndIsArchivedFalse(Long id);

    @Query("SELECT c FROM Colocation c WHERE c.isPublished = false")
    Page<Colocation> findNonPublishedColocations(Pageable pageable);


    @Query("SELECT c FROM Colocation c LEFT JOIN c.rules r LEFT JOIN c.tags t WHERE " +
            "(LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.nameOfPublisher) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.address) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.city) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.postalCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.status) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.roommatesGenderPreference) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "c.isPublished = false")
    Page<Colocation> searchNonPublished(@Param("keyword") String keyword, Pageable pageable);
    @Query("SELECT c FROM Colocation c LEFT JOIN c.rules r LEFT JOIN c.tags t WHERE " +
            "c.idOfPublisher = :idOfPublisher AND (" +
            ":keyword IS NULL OR :keyword = '' OR " + // permet d’ignorer le filtre si keyword est null/vide
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.nameOfPublisher) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.address) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.city) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.postalCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.status) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.roommatesGenderPreference) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Colocation> findOwnColocationsByKeyword(
            @Param("idOfPublisher") String idOfPublisher,
            @Param("keyword") String keyword,
            Pageable pageable);

}
