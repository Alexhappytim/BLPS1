package com.blps.app.domain.model;

import com.blps.app.infrastructure.messaging.mail.EmailCommandType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "email_dispatch_log")
public class EmailDispatchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailCommandType type;

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false)
    private OffsetDateTime dispatchedAt;

    protected EmailDispatchLog() {
    }

    public EmailDispatchLog(EmailCommandType type, String recipient, OffsetDateTime dispatchedAt) {
        this.type = type;
        this.recipient = recipient;
        this.dispatchedAt = dispatchedAt;
    }

    public Long getId() {
        return id;
    }

    public EmailCommandType getType() {
        return type;
    }

    public String getRecipient() {
        return recipient;
    }

    public OffsetDateTime getDispatchedAt() {
        return dispatchedAt;
    }
}
