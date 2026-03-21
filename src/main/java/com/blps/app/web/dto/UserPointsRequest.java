package com.blps.app.web.dto;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UserPointsRequest(
	@NotBlank String login,
	@NotNull @Positive Long courseId
) implements LoginCarrier {
}
