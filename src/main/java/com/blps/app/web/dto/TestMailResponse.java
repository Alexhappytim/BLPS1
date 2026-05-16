package com.blps.app.web.dto;

import java.util.UUID;

public record TestMailResponse(
        UUID messageId,
        String topic,
        String to
) {
}
