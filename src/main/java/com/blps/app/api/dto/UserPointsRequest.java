package com.blps.app.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UserPointsRequest(@NotBlank String login) implements LoginCarrier {
}
