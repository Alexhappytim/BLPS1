package com.blps.app.infrastructure.crm.dto;

public record CrmTaskReviewUpsertRequest(
        Long submissionId,
        String mentorLogin,
        boolean approved,
        Long mentorReward
) {
}
