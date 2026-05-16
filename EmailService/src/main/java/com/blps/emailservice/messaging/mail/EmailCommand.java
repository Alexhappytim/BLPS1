package com.blps.emailservice.messaging.mail;

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
}
