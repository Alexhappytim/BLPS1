package com.blps.app.infrastructure.notification;

import com.blps.app.domain.model.AppUser;
import com.blps.app.domain.model.Course;
import com.blps.app.infrastructure.messaging.mail.EmailCommandType;
import com.blps.app.infrastructure.messaging.mail.MailDispatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class KafkaCourseCertificateSender implements CourseCertificateSender {

    private static final Logger log = LoggerFactory.getLogger(KafkaCourseCertificateSender.class);

    private final MailDispatchService mailDispatchService;

    public KafkaCourseCertificateSender(MailDispatchService mailDispatchService) {
        this.mailDispatchService = mailDispatchService;
    }

    @Override
    public boolean sendCourseCompletionCertificate(AppUser user, Course course) {
        String to = user.getLogin();
        if (!mailDispatchService.isMailEnabled()) {
            log.info("Mail sending is disabled. Skip certificate for user={} course={}", user.getLogin(), course.getCode());
            return false;
        }
        if (!looksLikeEmail(to)) {
            log.warn("User login is not an email. Cannot dispatch certificate: login={}", to);
            return false;
        }

        boolean dispatched = mailDispatchService.dispatch(
                EmailCommandType.COURSE_CERTIFICATE,
                to,
                "Certificate for completed course: " + course.getTitle(),
                buildBody(user, course)
        );

        if (!dispatched) {
            log.warn("Failed to dispatch certificate email to {} for course {}", to, course.getCode());
        }
        return dispatched;
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
