package org.example.pfabackend.repositories;

import org.example.pfabackend.entities.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    // Optional: methods like findBySender, findByReceiver, etc.
}
