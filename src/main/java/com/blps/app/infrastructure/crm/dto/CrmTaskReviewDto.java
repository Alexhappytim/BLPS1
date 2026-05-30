package com.blps.app.infrastructure.crm.dto;

import java.time.OffsetDateTime;

public record CrmTaskReviewDto(
        Long submissionId,
        String mentorLogin,
        boolean approved,
        Long mentorReward,
        OffsetDateTime reviewedAt
) {
}
