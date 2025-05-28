package org.example.pfabackend.controllers;

import lombok.RequiredArgsConstructor;
import org.example.pfabackend.entities.ChatMessageEntity;
import org.example.pfabackend.repositories.ChatMessageRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatMessageRepository chatMessageRepository;

    @GetMapping("/messages")
    public List<ChatMessageEntity> getAllMessages() {
        return chatMessageRepository.findAll();
    }
    @GetMapping("/messages/private")
    public List<ChatMessageEntity> getPrivateMessages(
            @RequestParam String user1,
            @RequestParam String user2) {
        return chatMessageRepository.findBySenderAndReceiverIn(user1, user2);
    }

    @GetMapping("/users")
    public List<String> getAllUsers() {
        List<String> users = chatMessageRepository.findDistinctUsers();
        System.out.println("Fetched users: " + users);
        return users;
    }
}
