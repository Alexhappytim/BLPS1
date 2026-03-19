package com.blps.app.api.dto;

public record OpenBlockResponse(
        Long blockId,
        boolean alreadyOpened,
        long remainingPoints
) {
}
