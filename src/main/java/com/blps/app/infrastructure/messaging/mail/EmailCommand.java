package com.blps.app.infrastructure.messaging.mail;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EmailCommand(
        UUID id,
        EmailCommandType type,
        String to,
        String subject,
        String body,
        OffsetDateTime createdAt
) {
    public static EmailCommand of(EmailCommandType type, String to, String subject, String body) {
        return new EmailCommand(UUID.randomUUID(), type, to, subject, body, OffsetDateTime.now());
    }
}
