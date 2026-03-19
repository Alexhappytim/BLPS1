package com.blps.app.api.dto;

import com.blps.app.domain.entity.Difficulty;

public record CoefficientResponse(Difficulty difficulty, double coefficient) {
}
