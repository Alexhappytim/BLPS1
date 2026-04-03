package com.blps.app.web.dto;

import com.blps.app.domain.model.ReviewType;

public record TaskDto(
        Long id,
        Long courseId,
        String code,
        String title,
        Long blockId,
        long basePoints,
        ReviewType reviewType
) {
}
