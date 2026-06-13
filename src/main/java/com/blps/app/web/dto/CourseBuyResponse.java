package com.blps.app.web.dto;

public record CourseBuyResponse(
        String invoiceId,
        String paymentUrl
) {
}
