package com.blps.app.web.dto;

import com.blps.app.domain.model.AppUserRole;

public record UserSummaryDto(Long id, String login, AppUserRole role) {
}
