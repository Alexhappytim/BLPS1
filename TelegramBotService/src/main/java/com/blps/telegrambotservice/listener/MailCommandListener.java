package com.blps.telegrambotservice.listener;

import com.blps.telegrambotservice.bot.TelegramBot;
import com.blps.telegrambotservice.domain.repository.TelegramUserLinkRepository;
import com.blps.telegrambotservice.messaging.mail.EmailCommand;
import com.blps.telegrambotservice.messaging.mail.EmailCommandType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MailCommandListener {

    private final ObjectMapper objectMapper;
    private final TelegramUserLinkRepository telegramUserLinkRepository;
    private final TelegramBot telegramBot;

    public MailCommandListener(
            ObjectMapper objectMapper,
            TelegramUserLinkRepository telegramUserLinkRepository,
            TelegramBot telegramBot
    ) {
        this.objectMapper = objectMapper;
        this.telegramUserLinkRepository = telegramUserLinkRepository;
        this.telegramBot = telegramBot;
    }

    @KafkaListener(
            topics = "${app.kafka.mail-topic:mail.send}",
            groupId = "${spring.kafka.consumer.group-id:telegram-bot-service}"
    )
    public void onMessage(String payload) throws Exception {
        EmailCommand command = objectMapper.readValue(payload, EmailCommand.class);
        if (command.type() != EmailCommandType.COURSE_CERTIFICATE) {
            return;
        }

        telegramUserLinkRepository.findByEmail(command.to())
                .ifPresent(link -> telegramBot.sendText(
                        link.getChatId(),
                        "Поздравляем! Вы успешно завершили курс. Сертификат отправлен на почту: " + command.to()
                ));
    }
}
