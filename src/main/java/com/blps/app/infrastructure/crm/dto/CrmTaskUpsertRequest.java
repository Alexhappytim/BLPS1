package com.blps.app.infrastructure.crm.dto;

public record CrmTaskUpsertRequest(
        String code,
        String title,
        long basePoints,
        String reviewType,
        Long mentorReviewReward
) {
}
