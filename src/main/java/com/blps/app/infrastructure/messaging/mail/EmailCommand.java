package com.blps.app.infrastructure.messaging.mail;

import java.time.OffsetDateTime;
import java.util.UUID;
public record EmailCommand(
        UUID id,
        EmailCommandType type,
        String to,
        String subject,
        String body,
        String imageUrl,
        OffsetDateTime createdAt
) {
    public static EmailCommand of(EmailCommandType type, String to, String subject, String body, String imageUrl) {
        return new EmailCommand(UUID.randomUUID(), type, to, subject, body, imageUrl, OffsetDateTime.now());
    }
}
