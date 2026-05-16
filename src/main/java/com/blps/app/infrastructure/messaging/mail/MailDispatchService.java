package com.blps.app.infrastructure.messaging.mail;

import com.blps.app.domain.model.EmailDispatchLog;
import com.blps.app.domain.repository.EmailDispatchLogRepository;
import com.blps.app.infrastructure.transaction.AfterCommitExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class MailDispatchService {

    private final KafkaMailPublisher kafkaMailPublisher;
    private final AfterCommitExecutor afterCommitExecutor;
    private final EmailDispatchLogRepository emailDispatchLogRepository;
    private final boolean mailEnabled;

    public MailDispatchService(KafkaMailPublisher kafkaMailPublisher,
                              AfterCommitExecutor afterCommitExecutor,
                              EmailDispatchLogRepository emailDispatchLogRepository,
                              @Value("${app.mail.enabled:true}") boolean mailEnabled) {
        this.kafkaMailPublisher = kafkaMailPublisher;
        this.afterCommitExecutor = afterCommitExecutor;
        this.emailDispatchLogRepository = emailDispatchLogRepository;
        this.mailEnabled = mailEnabled;
    }

    public boolean isMailEnabled() {
        return mailEnabled;
    }

    @Transactional
    public boolean dispatch(EmailCommandType type, String to, String subject, String body) {
        if (!mailEnabled) {
            return false;
        }
        EmailCommand command = EmailCommand.of(type, to, subject, body);
        afterCommitExecutor.run(() -> kafkaMailPublisher.publish(command));
        emailDispatchLogRepository.save(new EmailDispatchLog(type, to, OffsetDateTime.now()));
        return true;
    }
}
