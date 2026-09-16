package com.blps.app.application.bpmn.delegates;

import com.blps.app.domain.model.AppUser;
import com.blps.app.domain.model.Course;
import com.blps.app.domain.model.CoursePurchase;
import com.blps.app.domain.model.CoursePurchaseStatus;
import com.blps.app.domain.model.UserCourseProgress;
import com.blps.app.domain.repository.AppUserRepository;
import com.blps.app.domain.repository.CoursePurchaseRepository;
import com.blps.app.domain.repository.CourseRepository;
import com.blps.app.domain.repository.UserCourseProgressRepository;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("grantCourseAccessDelegate")
public class GrantCourseAccessDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(GrantCourseAccessDelegate.class);

    private final AppUserRepository appUserRepository;
    private final CourseRepository courseRepository;
    private final CoursePurchaseRepository coursePurchaseRepository;
    private final UserCourseProgressRepository userCourseProgressRepository;

    public GrantCourseAccessDelegate(AppUserRepository appUserRepository,
                                   CourseRepository courseRepository,
                                   CoursePurchaseRepository coursePurchaseRepository,
                                   UserCourseProgressRepository userCourseProgressRepository) {
        this.appUserRepository = appUserRepository;
        this.courseRepository = courseRepository;
        this.coursePurchaseRepository = coursePurchaseRepository;
        this.userCourseProgressRepository = userCourseProgressRepository;
    }

    @Override
    @Transactional
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

        log.info("Executing GrantCourseAccessDelegate (Activity_1or579w) for user [{}] and courseId [{}]",
                login, courseId);

        AppUser user = login != null ? appUserRepository.findByLogin(login).orElse(null) : null;
        Course course = courseRepository.findById(courseId).orElse(null);

        if (user == null || course == null) {
            log.warn("User or Course not found when granting course access: user [{}], courseId [{}]", login, courseId);
            execution.setVariable("courseAccessGranted", true);
            return;
        }

        // 1. Mark purchase as PAID if pending
        CoursePurchase purchase = coursePurchaseRepository.findByUserAndCourse(user, course).orElse(null);
        if (purchase != null && purchase.getStatus() != CoursePurchaseStatus.PAID) {
            purchase.markPaid();
            coursePurchaseRepository.save(purchase);
            log.info("Marked purchase as PAID for user [{}] and course [{}]", login, course.getCode());
        }

        // 2. Ensure UserCourseProgress exists so user is enrolled and can progress
        if (userCourseProgressRepository.findByUserAndCourse(user, course).isEmpty()) {
            UserCourseProgress newProgress = new UserCourseProgress(user, course);
            userCourseProgressRepository.save(newProgress);
            log.info("Created UserCourseProgress for user [{}] in course [{}]", user.getLogin(), course.getCode());
        }

        execution.setVariable("courseAccessGranted", true);
        execution.setVariable("enrolled", true);
        log.info("Successfully granted course access for user [{}] to course [{}] ({})",
                login, courseId, course.getTitle());
    }
}
