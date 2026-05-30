package com.blps.app.infrastructure.messaging.auth;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TelegramRegisterUserCommand(
        UUID requestId,
        Long chatId,
        String email,
        String password,
        OffsetDateTime createdAt
) {
}
