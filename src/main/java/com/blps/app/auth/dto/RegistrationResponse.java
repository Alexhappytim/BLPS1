package com.blps.app.auth.dto;

import com.blps.app.domain.model.AppUserRole;

public record RegistrationResponse(
        String email,
        AppUserRole role,
        boolean emailConfirmationRequired,
        String verificationToken,
        String message
) {
}
