package com.blps.app.infrastructure.notification;

import com.blps.app.domain.model.AppUser;
import com.blps.app.domain.model.Course;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class SmtpCourseCertificateSender implements CourseCertificateSender {

    private static final Logger log = LoggerFactory.getLogger(SmtpCourseCertificateSender.class);

    private final JavaMailSender javaMailSender;
    private final boolean mailEnabled;
    private final String fromAddress;

    public SmtpCourseCertificateSender(JavaMailSender javaMailSender,
                                       @Value("${app.mail.enabled:false}") boolean mailEnabled,
                                       @Value("${app.mail.from:${spring.mail.username:}}") String fromAddress) {
        this.javaMailSender = javaMailSender;
        this.mailEnabled = mailEnabled;
        this.fromAddress = fromAddress;
    }

    @Override
    public boolean sendCourseCompletionCertificate(AppUser user, Course course) {
        String to = user.getLogin();
        if (!mailEnabled) {
            log.info("Mail sending is disabled. Skip certificate for user={} course={}", user.getLogin(), course.getCode());
            return false;
        }
        if (!looksLikeEmail(to)) {
            log.warn("User login is not an email. Cannot send certificate: login={}", to);
            return false;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            if (fromAddress != null && !fromAddress.isBlank()) {
                message.setFrom(fromAddress);
            }
            message.setSubject("Certificate for completed course: " + course.getTitle());
            message.setText(buildBody(user, course));
            javaMailSender.send(message);
            return true;
        } catch (Exception ex) {
            log.error("Failed to send certificate email to {} for course {}", to, course.getCode(), ex);
            return false;
        }
    }

    private boolean looksLikeEmail(String value) {
        return value != null && value.contains("@") && value.indexOf('@') > 0;
    }

    private String buildBody(AppUser user, Course course) {
        return "Hello, " + user.getLogin() + "!\n\n"
                + "Congratulations! You have successfully completed the course '" + course.getTitle() + "'.\n"
                + "Certificate code: " + course.getCode() + "-" + user.getId() + "\n\n"
                + "Best regards,\nBLPS platform";
    }
}
