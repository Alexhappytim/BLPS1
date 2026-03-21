package com.blps.app.web.dto;

public record TaskDto(
        Long id,
        Long courseId,
        String code,
        String title,
        Long blockId,
        long basePoints,
        boolean requiresMentorReview
) {
}
