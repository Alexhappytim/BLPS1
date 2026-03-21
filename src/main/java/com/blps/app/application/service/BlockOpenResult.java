package com.blps.app.application.service;

public record BlockOpenResult(
        Long blockId,
        boolean alreadyOpened,
        long remainingPoints
) {
}
