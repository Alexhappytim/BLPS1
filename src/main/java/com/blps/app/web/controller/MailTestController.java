package com.blps.app.web.controller;

import com.blps.app.domain.model.EmailDispatchLog;
import com.blps.app.domain.repository.EmailDispatchLogRepository;
import com.blps.app.infrastructure.messaging.mail.EmailCommand;
import com.blps.app.infrastructure.messaging.mail.EmailCommandType;
import com.blps.app.infrastructure.scheduling.InactivityReminderScheduler;
import com.blps.app.web.dto.TestMailRequest;
import com.blps.app.web.dto.TestMailResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/mail")
public class MailTestController {
    private final String mailTopic;
    @Autowired
    private InactivityReminderScheduler scheduler;
    public MailTestController(@Value("${app.kafka.mail-topic:mail.send}") String mailTopic) {
        this.mailTopic = mailTopic;
    }

    @PostMapping("/test")
    public TestMailResponse sendTestMail(@Valid @RequestBody TestMailRequest request) {
        scheduler.sendMonthlyInactivityReminders();
        return new TestMailResponse(UUID.randomUUID(), mailTopic, request.to());
    }
}
