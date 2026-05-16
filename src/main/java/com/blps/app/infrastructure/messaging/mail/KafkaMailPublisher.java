package com.blps.app.infrastructure.messaging.mail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KafkaMailPublisher {

    private final KafkaProducer<String, String> kafkaProducer;
    private final ObjectMapper objectMapper;
    private final String mailTopic;

    public KafkaMailPublisher(KafkaProducer<String, String> kafkaProducer,
                              ObjectMapper objectMapper,
                              @Value("${app.kafka.mail-topic:mail.send}") String mailTopic) {
        this.kafkaProducer = kafkaProducer;
        this.objectMapper = objectMapper;
        this.mailTopic = mailTopic;
    }

    public void publish(EmailCommand command) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(command);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize EmailCommand to JSON", e);
        }

        kafkaProducer.send(new ProducerRecord<>(mailTopic, command.to(), payload));
    }
}
