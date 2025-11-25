package com.example.studybot.model;

import jakarta.persistence.*;


import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")


public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userEmail;

    @Column(columnDefinition = "TEXT")
    private String message;

    private boolean fromBot;

    private LocalDateTime createdAt = LocalDateTime.now();

    public ChatMessage(Long id, String userEmail, String message, boolean fromBot, LocalDateTime createdAt) {
        this.id = id;
        this.userEmail = userEmail;
        this.message = message;
        this.fromBot = fromBot;
        this.createdAt = createdAt;
    }

    public ChatMessage() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isFromBot() {
        return fromBot;
    }

    public void setFromBot(boolean fromBot) {
        this.fromBot = fromBot;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
