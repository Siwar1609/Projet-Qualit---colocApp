package org.example.pfabackend.controllers;

import lombok.RequiredArgsConstructor;
import org.example.pfabackend.dto.ChatMessage;
import org.example.pfabackend.entities.ChatMessageEntity;
import org.example.pfabackend.repositories.ChatMessageRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatMessageRepository chatMessageRepository;

    @GetMapping("/messages")
    public List<ChatMessage> getAllMessages() {
        return chatMessageRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/messages/private")
    public List<ChatMessage> getPrivateMessages(
            @RequestParam String user1,
            @RequestParam String user2) {
        return chatMessageRepository.findBySenderAndReceiverIn(user1, user2).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/users")
    public List<String> getAllUsers() {
        List<String> users = chatMessageRepository.findDistinctUsers();
        System.out.println("Fetched users: " + users);
        return users;
    }

    @GetMapping("/messages/colocation")
    public List<ChatMessage> getMessagesByColocation(@RequestParam Long colocationId) {
        return chatMessageRepository.findByColocationId(colocationId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private ChatMessage toDto(ChatMessageEntity entity) {
        return ChatMessage.builder()
                .message(entity.getMessage())
                .sender(entity.getSender())
                .receiver(entity.getReceiver())
                .date(entity.getDate())
                .type(entity.getType())
                .idColoc(entity.getColocation() != null ? entity.getColocation().getId() : null)
                .build();
    }
}