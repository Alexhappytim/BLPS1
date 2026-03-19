package com.blps.app.domain.service;

public record BlockOpenResult(
        Long blockId,
        boolean alreadyOpened,
        long remainingPoints
) {
}
