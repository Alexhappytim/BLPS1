package com.blps.app.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OpenBlockRequest(
        @NotBlank String login,
        @NotNull @Positive Long blockId
) implements LoginCarrier {
}
