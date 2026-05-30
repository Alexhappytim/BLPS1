package com.blps.telegrambotservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic authRegisterTopic(@Value("${app.kafka.auth-register-topic}") String name) {
        return TopicBuilder.name(name).build();
    }

    @Bean
    public NewTopic authRegisterResultTopic(@Value("${app.kafka.auth-register-result-topic}") String name) {
        return TopicBuilder.name(name).build();
    }
}
