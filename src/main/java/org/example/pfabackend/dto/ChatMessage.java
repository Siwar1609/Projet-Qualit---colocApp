package org.example.pfabackend.dto;

import lombok.*;

import java.util.Date;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {
    private String message;
    private String sender;
    private String receiver;
    private Date date;
    private  MessageType type;
}
