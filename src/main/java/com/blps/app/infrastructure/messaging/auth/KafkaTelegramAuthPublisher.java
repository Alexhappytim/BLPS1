package com.blps.app.infrastructure.messaging.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KafkaTelegramAuthPublisher {

    private final KafkaProducer<String, String> kafkaProducer;
    private final ObjectMapper objectMapper;
    private final String resultTopic;

    public KafkaTelegramAuthPublisher(
            KafkaProducer<String, String> kafkaProducer,
            ObjectMapper objectMapper,
            @Value("${app.kafka.auth-register-result-topic:auth.register.telegram.result}") String resultTopic
    ) {
        this.kafkaProducer = kafkaProducer;
        this.objectMapper = objectMapper;
        this.resultTopic = resultTopic;
    }

    public void publish(TelegramRegisterUserResult result) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize TelegramRegisterUserResult to JSON", e);
        }

        kafkaProducer.send(new ProducerRecord<>(resultTopic, result.requestId().toString(), payload));
    }
}
