package com.blps.app.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record BlockUpsertRequest(
        @NotNull @Positive Long courseId,
        @NotBlank String code,
        @NotBlank String title,
        @NotNull @PositiveOrZero Long openCost,
        @NotNull @Positive Integer orderIndex
) {
}
