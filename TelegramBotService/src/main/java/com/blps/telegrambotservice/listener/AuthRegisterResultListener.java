package com.blps.telegrambotservice.listener;

import com.blps.telegrambotservice.bot.TelegramBot;
import com.blps.telegrambotservice.domain.TelegramUserLink;
import com.blps.telegrambotservice.domain.repository.TelegramUserLinkRepository;
import com.blps.telegrambotservice.messaging.auth.TelegramRegisterUserResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AuthRegisterResultListener {

    private final ObjectMapper objectMapper;
    private final TelegramBot telegramBot;
    private final TelegramUserLinkRepository telegramUserLinkRepository;

    public AuthRegisterResultListener(
            ObjectMapper objectMapper,
            TelegramBot telegramBot,
            TelegramUserLinkRepository telegramUserLinkRepository
    ) {
        this.objectMapper = objectMapper;
        this.telegramBot = telegramBot;
        this.telegramUserLinkRepository = telegramUserLinkRepository;
    }

    @KafkaListener(topics = "${app.kafka.auth-register-result-topic}")
    public void onMessage(String payload) throws Exception {
        TelegramRegisterUserResult result = objectMapper.readValue(payload, TelegramRegisterUserResult.class);

        if (result.success()) {
            upsertLink(result.email(), result.chatId());
            telegramBot.sendText(result.chatId(), "Регистрация успешна: " + result.message());
        } else {
            telegramBot.sendText(result.chatId(), "Регистрация не удалась: " + result.message());
        }
    }

    private void upsertLink(String email, Long chatId) {
        telegramUserLinkRepository.findByEmail(email)
                .ifPresentOrElse(
                        link -> {
                            link.updateChatId(chatId);
                            telegramUserLinkRepository.save(link);
                        },
                        () -> telegramUserLinkRepository.save(new TelegramUserLink(email, chatId))
                );
    }
}
