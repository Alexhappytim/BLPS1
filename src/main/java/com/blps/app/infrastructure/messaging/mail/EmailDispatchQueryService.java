package com.blps.app.infrastructure.messaging.mail;

import com.blps.app.domain.repository.EmailDispatchLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class EmailDispatchQueryService {

    private final EmailDispatchLogRepository emailDispatchLogRepository;

    public EmailDispatchQueryService(EmailDispatchLogRepository emailDispatchLogRepository) {
        this.emailDispatchLogRepository = emailDispatchLogRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Duration> timeSinceLastDispatch(String recipient, EmailCommandType type) {
        return emailDispatchLogRepository.findTopByRecipientAndTypeOrderByDispatchedAtDesc(recipient, type)
                .map(log -> Duration.between(log.getDispatchedAt(), OffsetDateTime.now()));
    }
}
