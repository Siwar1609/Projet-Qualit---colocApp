package org.example.pfabackend.entities;

import jakarta.persistence.*;
import lombok.*;
import org.example.pfabackend.enums.MessageType;

import java.util.Date;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    private String sender;

    private String receiver;

    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @Enumerated(EnumType.STRING)
    private MessageType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_coloc", nullable = true)
    private Colocation colocation;
}