package com.blps.telegrambotservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka")
public record AppKafkaProperties(
        String mailTopic,
        String authRegisterTopic,
        String authRegisterResultTopic
) {
}
