package com.blps.telegrambotservice.bot;

import com.blps.telegrambotservice.config.TelegramProperties;
import com.blps.telegrambotservice.messaging.auth.TelegramRegisterUserCommand;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final TelegramProperties telegramProperties;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private final String authRegisterTopic;

    public TelegramBot(
            TelegramProperties telegramProperties,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${app.kafka.auth-register-topic}") String authRegisterTopic
    ) {
        super(telegramProperties.botToken());
        this.telegramProperties = telegramProperties;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.authRegisterTopic = authRegisterTopic;
    }

    @Override
    public String getBotUsername() {
        return telegramProperties.botUsername();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage()) {
            return;
        }
        Message message = update.getMessage();
        if (!message.hasText()) {
            return;
        }

        String text = message.getText().trim();
        Long chatId = message.getChatId();

        if (text.startsWith("/start")) {
            sendText(chatId, "Привет! Для регистрации используй: /register email password");
            return;
        }

        if (text.startsWith("/register")) {
            handleRegister(chatId, text);
            return;
        }

        sendText(chatId, "Неизвестная команда. Доступно: /register");
    }

    private void handleRegister(Long chatId, String text) {
        String[] parts = text.split("\\s+");
        if (parts.length < 3) {
            sendText(chatId, "Формат: /register email password");
            return;
        }

        String email = parts[1];
        String password = parts[2];

        TelegramRegisterUserCommand cmd = TelegramRegisterUserCommand.of(chatId, email, password);
        String payload;
        try {
            payload = objectMapper.writeValueAsString(cmd);
        } catch (JsonProcessingException e) {
            sendText(chatId, "Ошибка сериализации запроса. Попробуй позже.");
            return;
        }

        kafkaTemplate.send(authRegisterTopic, cmd.requestId().toString(), payload);
        sendText(chatId, "Запрос на регистрацию отправлен. Жду результат...");
    }

    public void sendText(Long chatId, String text) {
        try {
            execute(SendMessage.builder().chatId(chatId.toString()).text(text).build());
        } catch (TelegramApiException ignored) {
            // no-op
        }
    }
}
