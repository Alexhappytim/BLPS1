package com.blps.emailservice.messaging.mail;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class MailCommandListener {

    private static final Logger log = LoggerFactory.getLogger(MailCommandListener.class);

    private final ObjectMapper objectMapper;
    private final JavaMailSender javaMailSender;
    private final boolean mailEnabled;
    private final String fromAddress;

    public MailCommandListener(ObjectMapper objectMapper,
                              JavaMailSender javaMailSender,
                              @Value("${app.mail.enabled:true}") boolean mailEnabled,
                              @Value("${app.mail.from:${spring.mail.username:}}") String fromAddress) {
        this.objectMapper = objectMapper;
        this.javaMailSender = javaMailSender;
        this.mailEnabled = mailEnabled;
        this.fromAddress = fromAddress;
    }

    @KafkaListener(
            topics = "${app.kafka.mail-topic:mail.send}",
            groupId = "${spring.kafka.consumer.group-id:email-service}"
    )
    public void onMessage(String payload) throws Exception {
        EmailCommand command = objectMapper.readValue(payload, EmailCommand.class);

        if (!mailEnabled) {
            log.info("Mail is disabled. Skip command id={} type={} to={}", command.id(), command.type(), command.to());
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(command.to());
        if (fromAddress != null && !fromAddress.isBlank()) {
            message.setFrom(fromAddress);
        }
        message.setSubject(command.subject());
        message.setText(command.body());

        javaMailSender.send(message);
        log.info("Sent email id={} type={} to={}", command.id(), command.type(), command.to());
    }
}
