package com.blps.app.auth.dto;

import com.blps.app.domain.model.AppUserRole;

public record AuthUserResponse(
        String email,
        AppUserRole role,
        boolean emailVerified,
        boolean enabled
) {
}
