package com.blps.app.api.dto;

import com.blps.app.domain.entity.SubmissionStatus;

public record SubmissionResponse(
        Long submissionId,
        SubmissionStatus status,
        long calculatedPoints,
        Long awardedPoints,
        long userPoints
) {
}
