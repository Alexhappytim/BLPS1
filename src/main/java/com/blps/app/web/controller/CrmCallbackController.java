package com.blps.app.web.controller;

import com.blps.app.application.service.LearningPlatformService;
import com.blps.app.web.dto.CrmPaymentCallbackRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crm")
public class CrmCallbackController {

    private final LearningPlatformService learningPlatformService;

    public CrmCallbackController(LearningPlatformService learningPlatformService) {
        this.learningPlatformService = learningPlatformService;
    }

    @PostMapping("/payment-callback")
    public void paymentCallback(@Valid @RequestBody CrmPaymentCallbackRequest request) {
        learningPlatformService.handlePaymentCallback(request.invoiceId(), request.success());
    }
}
