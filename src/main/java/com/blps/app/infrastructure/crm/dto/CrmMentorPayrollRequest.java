package com.blps.app.infrastructure.crm.dto;

public record CrmMentorPayrollRequest(
        String mentorLogin,
        long amount,
        String reason
) {
}
