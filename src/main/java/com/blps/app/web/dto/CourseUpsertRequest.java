package com.blps.app.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record CourseUpsertRequest(
        @NotBlank String code,
        @NotBlank String title,
        @PositiveOrZero long price
) {
}
