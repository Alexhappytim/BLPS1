package com.blps.app.api.dto;

public record TaskDto(
        Long id,
        String code,
        String title,
        Long blockId,
        long basePoints,
        boolean requiresMentorReview
) {
}
