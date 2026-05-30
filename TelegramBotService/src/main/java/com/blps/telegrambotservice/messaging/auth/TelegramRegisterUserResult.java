package com.blps.telegrambotservice.messaging.auth;

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
}
