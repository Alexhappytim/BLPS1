package com.blps.telegrambotservice.config;

import com.blps.telegrambotservice.bot.TelegramBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class TelegramBotsConfig {

    private static final Logger log = LoggerFactory.getLogger(TelegramBotsConfig.class);

    public TelegramBotsConfig(TelegramProperties telegramProperties, TelegramBot bot) throws Exception {
        if (telegramProperties.botToken() == null || telegramProperties.botToken().isBlank()
                || telegramProperties.botUsername() == null || telegramProperties.botUsername().isBlank()) {
            log.warn("Telegram bot is not configured (token/username empty). Skipping bot registration.");
            return;
        }

        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(bot);
        log.info("Telegram bot registered as @{}", telegramProperties.botUsername());
    }
}
