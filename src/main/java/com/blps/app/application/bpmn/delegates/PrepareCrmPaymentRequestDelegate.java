package com.blps.app.application.bpmn.delegates;

import com.blps.app.domain.model.AppUser;
import com.blps.app.domain.model.Course;
import com.blps.app.domain.repository.AppUserRepository;
import com.blps.app.domain.repository.CourseRepository;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("prepareCrmPaymentRequestDelegate")
public class PrepareCrmPaymentRequestDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(PrepareCrmPaymentRequestDelegate.class);

    private final AppUserRepository appUserRepository;
    private final CourseRepository courseRepository;

    public PrepareCrmPaymentRequestDelegate(AppUserRepository appUserRepository,
                                           CourseRepository courseRepository) {
        this.appUserRepository = appUserRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String login = (String) execution.getVariable("login");
        if (login == null || login.isBlank()) {
            login = execution.getProcessBusinessKey();
            if (login != null && login.contains(":")) {
                login = login.substring(0, login.indexOf(":"));
            }
        }

        Object courseIdVar = execution.getVariable("courseId");
        Long courseId = 1L;
        if (courseIdVar instanceof Number num) {
            courseId = num.longValue();
        } else if (courseIdVar instanceof String str && !str.isBlank()) {
            try {
                courseId = Long.parseLong(str.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        log.info("Executing PrepareCrmPaymentRequestDelegate (Activity_13adnc1) for user [{}] and courseId [{}]",
                login, courseId);

        AppUser user = login != null ? appUserRepository.findByLogin(login).orElse(null) : null;
        Course course = courseRepository.findById(courseId).orElse(null);

        if (course != null) {
            execution.setVariable("courseId", course.getId());
            execution.setVariable("courseCode", course.getCode());
            execution.setVariable("courseTitle", course.getTitle());
            execution.setVariable("coursePrice", course.getPrice());
            execution.setVariable("crmRequestPrepared", true);
            log.info("Prepared CRM payment request for user [{}]: course [{}] ({}), price [{}]",
                    login, course.getCode(), course.getTitle(), course.getPrice());
        } else {
            log.warn("Course with id [{}] not found when preparing CRM request", courseId);
            execution.setVariable("crmRequestPrepared", false);
        }
    }
}
