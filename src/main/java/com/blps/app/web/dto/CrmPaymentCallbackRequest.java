package com.blps.app.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CrmPaymentCallbackRequest(
        @NotBlank String invoiceId,
        @NotNull Boolean success
) {
}
