package com.blps.app.auth;

import com.blps.app.domain.model.AppUser;
import com.blps.app.domain.model.AppUserRole;

public record AuthenticatedUser(Long id, String login, AppUserRole role) {

    public static AuthenticatedUser fromEntity(AppUser user) {
        return new AuthenticatedUser(user.getId(), user.getLogin(), user.getRole());
    }
}
