package com.blps.app.web.dto;

import com.blps.app.domain.model.SubmissionStatus;

public record SubmissionResponse(
        Long submissionId,
        SubmissionStatus status,
        long calculatedPoints,
        Long awardedPoints,
        long userPoints
) {
}
