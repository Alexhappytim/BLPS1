package com.blps.app.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CourseUpsertRequest(
        @NotBlank String code,
        @NotBlank String title
) {
}
