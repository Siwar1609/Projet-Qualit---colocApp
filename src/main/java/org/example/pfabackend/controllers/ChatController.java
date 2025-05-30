package org.example.pfabackend.controllers;

import lombok.RequiredArgsConstructor;
import org.example.pfabackend.dto.ChatMessage;
import org.example.pfabackend.enums.MessageType;
import org.example.pfabackend.entities.ChatMessageEntity;
import org.example.pfabackend.entities.Colocation;
import org.example.pfabackend.repositories.ChatMessageRepository;
import org.example.pfabackend.repositories.ColocationRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.Date;

@CrossOrigin(origins = "*")
@Controller
@RequiredArgsConstructor
public class ChatController {

   private final ChatMessageRepository chatMessageRepository;
   private final ColocationRepository colocationRepository; // Added repository
   private final SimpMessageSendingOperations messagingTemplate;

   @MessageMapping("/chat.sendMessage")
   public void sendMessage(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
      chatMessage.setDate(new Date());
      saveMessage(chatMessage);
      messagingTemplate.convertAndSend("/topic/public", chatMessage);
   }

   @MessageMapping("/chat.addUser")
   public void addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
      String username = chatMessage.getSender();
      headerAccessor.getSessionAttributes().put("username", username);
      chatMessage.setType(MessageType.JOIN);
      chatMessage.setDate(new Date());
      saveMessage(chatMessage);
      messagingTemplate.convertAndSend("/topic/public", chatMessage);
   }

   private void saveMessage(ChatMessage chatMessage) {
      // Fetch Colocation if idColoc is provided
      Colocation colocation = null;
      if (chatMessage.getIdColoc() != null) {
         colocation = colocationRepository.findById(chatMessage.getIdColoc())
                 .orElseThrow(() -> new IllegalArgumentException("Colocation with ID " + chatMessage.getIdColoc() + " not found"));
      }

      ChatMessageEntity entity = ChatMessageEntity.builder()
              .message(chatMessage.getMessage())
              .sender(chatMessage.getSender())
              .receiver(chatMessage.getReceiver())
              .date(chatMessage.getDate())
              .type(chatMessage.getType())
              .colocation(colocation) // Set Colocation entity (null if idColoc is null)
              .build();
      chatMessageRepository.save(entity);
   }
}