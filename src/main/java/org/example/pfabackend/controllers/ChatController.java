package org.example.pfabackend.controllers;

import lombok.RequiredArgsConstructor;
import org.example.pfabackend.dto.ChatMessage;
import org.example.pfabackend.entities.ChatMessageEntity;
import org.example.pfabackend.repositories.ChatMessageRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.Date;

@CrossOrigin(origins = "*")
@Controller
@RequiredArgsConstructor
public class ChatController {

   private final ChatMessageRepository chatMessageRepository;

   @MessageMapping("/chat.sendMessage")
   @SendTo("/topic/public")
   public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
      chatMessage.setDate(new Date());
      saveMessage(chatMessage);
      return chatMessage;
   }

   @MessageMapping("/chat.addUser")
   @SendTo("/topic/public")
   public ChatMessage addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
      headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
      chatMessage.setDate(new Date());
      saveMessage(chatMessage);
      return chatMessage;
   }

   private void saveMessage(ChatMessage chatMessage) {
      ChatMessageEntity entity = ChatMessageEntity.builder()
              .message(chatMessage.getMessage())
              .sender(chatMessage.getSender())
              .receiver(chatMessage.getReceiver())
              .date(chatMessage.getDate())
              .type(chatMessage.getType())
              .build();
      chatMessageRepository.save(entity);
   }
}
