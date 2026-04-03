package com.blps.app.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReviewSubmissionRequest(
        @NotBlank @Email String login,
        @NotNull @Positive Long courseId,
        @NotNull @Positive Long submissionId,
        @NotNull Boolean approved
) implements LoginCarrier {
}
