package com.blps.telegrambotservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "telegram_user_link")
public class TelegramUserLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private Long chatId;

    @Column(nullable = false)
    private OffsetDateTime linkedAt;

    protected TelegramUserLink() {
    }

    public TelegramUserLink(String email, Long chatId) {
        this.email = email;
        this.chatId = chatId;
        this.linkedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Long getChatId() {
        return chatId;
    }

    public OffsetDateTime getLinkedAt() {
        return linkedAt;
    }

    public void updateChatId(Long chatId) {
        this.chatId = chatId;
        this.linkedAt = OffsetDateTime.now();
    }
}
