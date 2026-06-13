package com.blps.app.infrastructure.crm.dto;

public record CrmUserUpsertRequest(
        Long id,
        String login,
        String role
) {}
