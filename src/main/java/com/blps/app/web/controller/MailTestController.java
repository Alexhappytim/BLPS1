package com.blps.app.web.controller;

import com.blps.app.domain.model.EmailDispatchLog;
import com.blps.app.domain.repository.EmailDispatchLogRepository;
import com.blps.app.infrastructure.messaging.mail.EmailCommand;
import com.blps.app.infrastructure.messaging.mail.EmailCommandType;
import com.blps.app.web.dto.TestMailRequest;
import com.blps.app.web.dto.TestMailResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/mail")
public class MailTestController {

    private final KafkaProducer<String, String> kafkaProducer;
    private final ObjectMapper objectMapper;
    private final EmailDispatchLogRepository emailDispatchLogRepository;
    private final String mailTopic;

    public MailTestController(KafkaProducer<String, String> kafkaProducer,
                              ObjectMapper objectMapper,
                              EmailDispatchLogRepository emailDispatchLogRepository,
                              @Value("${app.kafka.mail-topic:mail.send}") String mailTopic) {
        this.kafkaProducer = kafkaProducer;
        this.objectMapper = objectMapper;
        this.emailDispatchLogRepository = emailDispatchLogRepository;
        this.mailTopic = mailTopic;
    }

    @PostMapping("/test")
    public TestMailResponse sendTestMail(@Valid @RequestBody TestMailRequest request) {
        EmailCommand command = new EmailCommand(
                java.util.UUID.randomUUID(),
                EmailCommandType.TEST,
                request.to(),
                request.subject(),
                request.body(),
                OffsetDateTime.now()
        );

        try {
            String payload = objectMapper.writeValueAsString(command);
            kafkaProducer.send(new ProducerRecord<>(mailTopic, request.to(), payload));
            kafkaProducer.flush();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to send Kafka mail message", ex);
        }

        emailDispatchLogRepository.save(new EmailDispatchLog(EmailCommandType.TEST, request.to(), OffsetDateTime.now()));
        return new TestMailResponse(command.id(), mailTopic, request.to());
    }
}
