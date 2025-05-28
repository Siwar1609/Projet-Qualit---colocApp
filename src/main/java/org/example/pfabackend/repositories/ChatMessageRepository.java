package org.example.pfabackend.repositories;

import org.example.pfabackend.entities.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    // Optional: methods like findBySender, findByReceiver, etc.
    @Query("SELECT c FROM ChatMessageEntity c WHERE " +
            "(c.sender = :user1 AND c.receiver = :user2) OR " +
            "(c.sender = :user2 AND c.receiver = :user1)")
    List<ChatMessageEntity> findBySenderAndReceiverIn(@Param("user1") String user1, @Param("user2") String user2);
    @Query("SELECT DISTINCT sender FROM ChatMessageEntity WHERE sender IS NOT NULL")
    List<String> findDistinctUsers();
}