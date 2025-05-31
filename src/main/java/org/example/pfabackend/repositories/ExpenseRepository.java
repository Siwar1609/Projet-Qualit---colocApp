package org.example.pfabackend.repositories;

import org.example.pfabackend.entities.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    //List<Expense> findByColocationId(Long colocationId);
    List<Expense> findByPaidByUserEmail(String userEmail);
    @Query("SELECT DISTINCT e FROM Expense e JOIN e.shares s WHERE s.paid = false")
    List<Expense> findBySharesPaidFalse();
    @Query("SELECT e FROM Expense e WHERE e.colocation.id = :colocationId")
    List<Expense> findByColocationId(@Param("colocationId") Long colocationId);

    @Query("SELECT e FROM Expense e WHERE e.colocation.idOfPublisher = :publisherId")
    List<Expense> findByPublisherId(@Param("publisherId") String publisherId);


    @Query("SELECT e FROM Expense e " +
            "JOIN e.shares s " +
            "WHERE s.userId = :userId")
    List<Expense> findByShareUserId(@Param("userId") String userId);

    @Query("SELECT e FROM Expense e " +
            "JOIN e.shares s " +
            "WHERE e.colocation.id = :colocationId " +
            "AND (e.colocation.idOfPublisher = :userId OR s.userId = :userId)")
    List<Expense> findByColocationIdVisibleToUser(@Param("colocationId") Long colocationId,
                                                  @Param("userId") String userId);
    List<Expense> findByPaidByUserId(String paidByUserId);

}
