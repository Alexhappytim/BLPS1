package com.blps.app.web.dto;

import com.blps.app.domain.model.Difficulty;
import com.blps.app.domain.model.ReviewType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SubmitTaskRequest(
        @NotBlank String login,
        @NotNull @Positive Long courseId,
        @NotNull @Positive Long taskId,
        @NotNull Difficulty difficulty,
        @NotNull ReviewType reviewType
) implements LoginCarrier {
}
