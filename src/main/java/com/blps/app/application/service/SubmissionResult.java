package com.blps.app.application.service;

import com.blps.app.domain.model.SubmissionStatus;

public record SubmissionResult(
        Long submissionId,
        SubmissionStatus status,
        long calculatedPoints,
        Long awardedPoints,
        long userPoints
) {
}
