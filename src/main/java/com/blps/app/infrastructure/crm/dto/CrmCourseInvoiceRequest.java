package com.blps.app.infrastructure.crm.dto;

public record CrmCourseInvoiceRequest(
        String userLogin,
        String courseCode
) {
}
