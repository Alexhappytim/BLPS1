package com.blps.app.application.bpmn.delegates;

import com.blps.app.common.BusinessException;
import com.blps.app.domain.model.AppUser;
import com.blps.app.domain.model.Course;
import com.blps.app.domain.model.CourseBlock;
import com.blps.app.domain.model.UserBlockAccess;
import com.blps.app.domain.model.UserCourseProgress;
import com.blps.app.domain.repository.AppUserRepository;
import com.blps.app.domain.repository.CourseBlockRepository;
import com.blps.app.domain.repository.CourseRepository;
import com.blps.app.domain.repository.UserBlockAccessRepository;
import com.blps.app.domain.repository.UserCourseProgressRepository;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("deductPointsAndOpenBlockDelegate")
public class DeductPointsAndOpenBlockDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(DeductPointsAndOpenBlockDelegate.class);

    private final AppUserRepository appUserRepository;
    private final CourseRepository courseRepository;
    private final CourseBlockRepository courseBlockRepository;
    private final UserCourseProgressRepository userCourseProgressRepository;
    private final UserBlockAccessRepository userBlockAccessRepository;

    public DeductPointsAndOpenBlockDelegate(AppUserRepository appUserRepository,
                                           CourseRepository courseRepository,
                                           CourseBlockRepository courseBlockRepository,
                                           UserCourseProgressRepository userCourseProgressRepository,
                                           UserBlockAccessRepository userBlockAccessRepository) {
        this.appUserRepository = appUserRepository;
        this.courseRepository = courseRepository;
        this.courseBlockRepository = courseBlockRepository;
        this.userCourseProgressRepository = userCourseProgressRepository;
        this.userBlockAccessRepository = userBlockAccessRepository;
    }

    @Override
    @Transactional
    public void execute(DelegateExecution execution) throws Exception {
        String login = (String) execution.getVariable("login");
        Long courseId = getLongVariable(execution, "courseId");
        Long blockId = getLongVariable(execution, "blockId");

        log.info("Executing open block: login={}, courseId={}, blockId={}", login, courseId, blockId);

        if (login == null || blockId == null) {
            execution.setVariable("hasEnoughPoints", false);
            return;
        }

        AppUser user = appUserRepository.findByLogin(login)
                .orElseThrow(() -> new BusinessException("User is not authenticated"));
        CourseBlock block = courseBlockRepository.findByIdWithCourse(blockId)
                .orElseThrow(() -> new BusinessException("Block not found: " + blockId));
        Course course = courseId != null ? courseRepository.findById(courseId).orElse(block.getCourse()) : block.getCourse();

        UserCourseProgress progress = userCourseProgressRepository.findByUserAndCourse(user, course)
                .orElseGet(() -> userCourseProgressRepository.save(new UserCourseProgress(user, course)));

        if (userBlockAccessRepository.existsByUserAndBlock(user, block)) {
            execution.setVariable("hasEnoughPoints", true);
            execution.setVariable("userPoints", progress.getPoints());
            return;
        }

        if (progress.getPoints() < block.getOpenCost()) {
            execution.setVariable("hasEnoughPoints", false);
            return;
        }

        progress.subtractPoints(block.getOpenCost());
        userBlockAccessRepository.save(new UserBlockAccess(user, block));

        execution.setVariable("hasEnoughPoints", true);
        execution.setVariable("userPoints", progress.getPoints());
        log.info("Block {} unlocked for user {}. Remaining points: {}", blockId, login, progress.getPoints());
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
