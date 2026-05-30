package com.blps.app.infrastructure.crm.dto;

import java.time.OffsetDateTime;

public record CrmCourseInvoiceDto(
        String invoiceId,
        String userLogin,
        String courseCode,
        long amount,
        String status,
        String paymentUrl,
        OffsetDateTime createdAt
) {
}
