package com.blps.app.domain.repository;

import com.blps.app.domain.model.EmailDispatchLog;
import com.blps.app.infrastructure.messaging.mail.EmailCommandType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailDispatchLogRepository extends JpaRepository<EmailDispatchLog, Long> {

    Optional<EmailDispatchLog> findTopByRecipientAndTypeOrderByDispatchedAtDesc(String recipient, EmailCommandType type);
}
