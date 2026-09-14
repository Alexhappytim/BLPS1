package com.blps.app.application.bpmn.delegates;

import com.blps.app.domain.model.AppUser;
import com.blps.app.domain.model.Course;
import com.blps.app.domain.model.UserCourseProgress;
import com.blps.app.domain.repository.AppUserRepository;
import com.blps.app.domain.repository.CourseRepository;
import com.blps.app.domain.repository.UserCourseProgressRepository;
import com.blps.app.infrastructure.notification.CourseCertificateSender;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("checkAndSendCertificateDelegate")
public class CheckAndSendCertificateDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(CheckAndSendCertificateDelegate.class);

    private final AppUserRepository appUserRepository;
    private final CourseRepository courseRepository;
    private final UserCourseProgressRepository userCourseProgressRepository;
    private final CourseCertificateSender courseCertificateSender;

    public CheckAndSendCertificateDelegate(AppUserRepository appUserRepository,
                                          CourseRepository courseRepository,
                                          UserCourseProgressRepository userCourseProgressRepository,
                                          CourseCertificateSender courseCertificateSender) {
        this.appUserRepository = appUserRepository;
        this.courseRepository = courseRepository;
        this.userCourseProgressRepository = userCourseProgressRepository;
        this.courseCertificateSender = courseCertificateSender;
    }

    @Override
    @Transactional
    public void execute(DelegateExecution execution) throws Exception {
        String login = (String) execution.getVariable("login");
        Long courseId = getLongVariable(execution, "courseId");

        if (login == null || courseId == null) {
            return;
        }

        AppUser user = appUserRepository.findByLogin(login).orElse(null);
        Course course = courseRepository.findById(courseId).orElse(null);
        if (user == null || course == null) {
            return;
        }

        UserCourseProgress progress = userCourseProgressRepository.findByUserAndCourse(user, course).orElse(null);
        if (progress != null && !progress.isCertificateSent()) {
            boolean sent = courseCertificateSender.sendCourseCompletionCertificate(user, course);
            if (sent) {
                progress.markCertificateSent();
                execution.setVariable("certificateSent", true);
                log.info("Certificate successfully sent via email to user: {}", login);
            }
        }
    }

    private Long getLongVariable(DelegateExecution execution, String name) {
        Object val = execution.getVariable(name);
        if (val instanceof Number num) {
            return num.longValue();
        }
        if (val instanceof String str && !str.isBlank()) {
            return Long.parseLong(str.trim());
        }
        return null;
    }
}
