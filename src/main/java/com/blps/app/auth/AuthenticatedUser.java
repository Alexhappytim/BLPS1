package com.blps.app.auth;

import com.blps.app.domain.model.AppUser;

public record AuthenticatedUser(Long id, String login) {

    public static AuthenticatedUser fromEntity(AppUser user) {
        return new AuthenticatedUser(user.getId(), user.getLogin());
    }
}
