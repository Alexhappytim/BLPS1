package com.blps.app.infrastructure.crm.dto;

public record CrmCourseUpsertRequest(
        String code,
        String title,
        long price
) {
}
