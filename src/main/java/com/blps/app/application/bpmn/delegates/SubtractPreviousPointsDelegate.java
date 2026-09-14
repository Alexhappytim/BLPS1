package com.blps.app.application.bpmn.delegates;

import com.blps.app.domain.model.AppUser;
import com.blps.app.domain.model.LearningTask;
import com.blps.app.domain.model.SubmissionStatus;
import com.blps.app.domain.model.TaskSubmission;
import com.blps.app.domain.model.UserCourseProgress;
import com.blps.app.domain.repository.AppUserRepository;
import com.blps.app.domain.repository.LearningTaskRepository;
import com.blps.app.domain.repository.TaskSubmissionRepository;
import com.blps.app.domain.repository.UserCourseProgressRepository;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("subtractPreviousPointsDelegate")
public class SubtractPreviousPointsDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(SubtractPreviousPointsDelegate.class);

    private final AppUserRepository appUserRepository;
    private final LearningTaskRepository learningTaskRepository;
    private final TaskSubmissionRepository taskSubmissionRepository;
    private final UserCourseProgressRepository userCourseProgressRepository;

    public SubtractPreviousPointsDelegate(AppUserRepository appUserRepository,
                                          LearningTaskRepository learningTaskRepository,
                                          TaskSubmissionRepository taskSubmissionRepository,
                                          UserCourseProgressRepository userCourseProgressRepository) {
        this.appUserRepository = appUserRepository;
        this.learningTaskRepository = learningTaskRepository;
        this.taskSubmissionRepository = taskSubmissionRepository;
        this.userCourseProgressRepository = userCourseProgressRepository;
    }

    @Override
    @Transactional
    public void execute(DelegateExecution execution) throws Exception {
        String login = (String) execution.getVariable("login");
        Long courseId = getLongVariable(execution, "courseId");
        Long taskId = getLongVariable(execution, "taskId");

        if (login == null || taskId == null || courseId == null) {
            return;
        }

        AppUser user = appUserRepository.findByLogin(login).orElse(null);
        LearningTask task = learningTaskRepository.findByIdWithBlock(taskId).orElse(null);
        if (user == null || task == null) {
            return;
        }

        long previousAwarded = taskSubmissionRepository
                .findFirstByUserAndTaskAndTask_Block_Course_IdAndStatusOrderBySubmittedAtDesc(
                        user, task, courseId, SubmissionStatus.APPROVED)
                .map(TaskSubmission::getAwardedPoints)
                .orElse(0L);

        log.info("Found previously awarded points for retry: {} (login={}, taskId={})", previousAwarded, login, taskId);
        execution.setVariable("previousAwarded", previousAwarded);
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
