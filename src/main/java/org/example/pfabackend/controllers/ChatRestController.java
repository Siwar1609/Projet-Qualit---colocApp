package org.example.pfabackend.controllers;

import lombok.RequiredArgsConstructor;
import org.example.pfabackend.entities.ChatMessageEntity;
import org.example.pfabackend.repositories.ChatMessageRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatMessageRepository chatMessageRepository;

    @GetMapping("/messages")
    public List<ChatMessageEntity> getAllMessages() {
        return chatMessageRepository.findAll();
    }
}
