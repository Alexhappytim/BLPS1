package com.blps.app.web.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CrmPaymentCallbackRequest(
        @NotBlank String invoiceId,
        @NotNull Boolean success
) {
    @JsonCreator
    public CrmPaymentCallbackRequest(
            @JsonProperty("invoiceId") String invoiceId,
            @JsonProperty("success") Object successValue
    ) {
        this(
                invoiceId,
                parseSuccess(successValue)
        );
    }

    private static Boolean parseSuccess(Object val) {
        if (val instanceof Boolean b) {
            return b;
        }
        if (val instanceof String s) {
            return Boolean.parseBoolean(s.trim());
        }
        return false;
    }
}
