package com.blps.app.api.dto;

import com.blps.app.domain.entity.Difficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ResolveCoefficientRequest(
        @NotBlank String login,
        @NotNull Difficulty difficulty
) implements LoginCarrier {
}
