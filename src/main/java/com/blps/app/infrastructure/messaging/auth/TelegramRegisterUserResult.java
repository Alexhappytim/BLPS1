package com.blps.app.infrastructure.messaging.auth;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TelegramRegisterUserResult(
        UUID requestId,
        Long chatId,
        String email,
        boolean success,
        String message,
        OffsetDateTime createdAt
) {

    public static TelegramRegisterUserResult success(UUID requestId, Long chatId, String email, String message) {
        return new TelegramRegisterUserResult(requestId, chatId, email, true, message, OffsetDateTime.now());
    }

    public static TelegramRegisterUserResult failure(UUID requestId, Long chatId, String email, String message) {
        return new TelegramRegisterUserResult(requestId, chatId, email, false, message, OffsetDateTime.now());
    }
}
