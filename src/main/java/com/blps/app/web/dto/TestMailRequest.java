package com.blps.app.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record TestMailRequest(
        @NotBlank @Email String to,
        @NotBlank String subject,
        @NotBlank String body
) {
}
