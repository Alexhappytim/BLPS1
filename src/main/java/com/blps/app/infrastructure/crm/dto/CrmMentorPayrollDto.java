package com.blps.app.infrastructure.crm.dto;

import java.time.OffsetDateTime;

public record CrmMentorPayrollDto(
        String payrollId,
        String mentorLogin,
        long amount,
        String status,
        OffsetDateTime createdAt
) {
}
