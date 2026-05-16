package com.blps.app.web.dto;

import com.blps.app.domain.model.ReviewType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record TaskUpsertRequest(
        @NotNull @Positive Long blockId,
        @NotBlank String code,
        @NotBlank String title,
        @NotNull @Positive Long basePoints,
        @NotNull ReviewType reviewType,
        @PositiveOrZero Long mentorReviewReward
) {
}
