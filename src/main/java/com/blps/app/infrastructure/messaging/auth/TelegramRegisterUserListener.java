package com.blps.app.infrastructure.messaging.auth;

import com.blps.app.auth.AuthManagementService;
import com.blps.app.auth.dto.RegistrationResponse;
import com.blps.app.common.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TelegramRegisterUserListener {

    private static final Logger log = LoggerFactory.getLogger(TelegramRegisterUserListener.class);

    private final ObjectMapper objectMapper;
    private final AuthManagementService authManagementService;
    private final KafkaTelegramAuthPublisher publisher;

    public TelegramRegisterUserListener(
            ObjectMapper objectMapper,
            AuthManagementService authManagementService,
            KafkaTelegramAuthPublisher publisher
    ) {
        this.objectMapper = objectMapper;
        this.authManagementService = authManagementService;
        this.publisher = publisher;
    }

    @KafkaListener(
            topics = "${app.kafka.auth-register-topic:auth.register.telegram}",
            groupId = "blps-backend-auth"
    )
    public void onMessage(String payload) throws Exception {
        TelegramRegisterUserCommand command = objectMapper.readValue(payload, TelegramRegisterUserCommand.class);

        try {
            RegistrationResponse response = authManagementService.registerUser(command.email(), command.password());
            String message = response.message();
            if (response.verificationToken() != null && !response.verificationToken().isBlank()) {
                message = message + ". verificationToken=" + response.verificationToken();
            }

            publisher.publish(TelegramRegisterUserResult.success(command.requestId(), command.chatId(), response.email(), message));
            log.info("Telegram registration OK requestId={} email={}", command.requestId(), response.email());
        } catch (BusinessException e) {
            publisher.publish(TelegramRegisterUserResult.failure(command.requestId(), command.chatId(), command.email(), e.getMessage()));
            log.info("Telegram registration rejected requestId={} email={} reason={}", command.requestId(), command.email(), e.getMessage());
        } catch (Exception e) {
            publisher.publish(TelegramRegisterUserResult.failure(command.requestId(), command.chatId(), command.email(), "Internal error"));
            log.warn("Telegram registration failed requestId={} email={}", command.requestId(), command.email(), e);
        }
    }
}
