package com.blps.telegrambotservice.messaging.auth;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TelegramRegisterUserCommand(
        UUID requestId,
        Long chatId,
        String email,
        String password,
        OffsetDateTime createdAt
) {
    public static TelegramRegisterUserCommand of(Long chatId, String email, String password) {
        return new TelegramRegisterUserCommand(UUID.randomUUID(), chatId, email, password, OffsetDateTime.now());
    }
}
