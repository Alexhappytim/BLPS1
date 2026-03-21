package com.blps.app.web.dto;

import com.blps.app.domain.model.Difficulty;

public record CoefficientResponse(Difficulty difficulty, double coefficient) {
}
