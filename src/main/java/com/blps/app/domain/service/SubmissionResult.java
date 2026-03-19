package com.blps.app.domain.service;

import com.blps.app.domain.entity.SubmissionStatus;

public record SubmissionResult(
        Long submissionId,
        SubmissionStatus status,
        long calculatedPoints,
        Long awardedPoints,
        long userPoints
) {
}
