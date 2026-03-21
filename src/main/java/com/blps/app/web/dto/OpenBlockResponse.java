package com.blps.app.web.dto;

public record OpenBlockResponse(
        Long blockId,
        boolean alreadyOpened,
        long remainingPoints
) {
}
