package com.blps.app.web.dto;

public record CourseCertificateSendResponse(
        String login,
        Long courseId,
        boolean sent
) {
}
