package com.blps.telegrambotservice.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({AppKafkaProperties.class, TelegramProperties.class})
public class AppConfig {
}
