package org.example.pfabackend.websocket;


import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketNotifier {

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyUser(String userId, String message) {
        // Envoie un message à /queue/notifications pour cet utilisateur
        messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", message);
    }
}
