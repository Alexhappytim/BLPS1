package com.blps.app.application.bpmn.delegates;

import com.blps.app.common.BusinessException;
import com.blps.app.domain.model.AppUser;
import com.blps.app.domain.model.Course;
import com.blps.app.domain.model.Difficulty;
import com.blps.app.domain.model.LearningTask;
import com.blps.app.domain.model.SubmissionStatus;
import com.blps.app.domain.model.TaskSubmission;
import com.blps.app.domain.model.UserCourseProgress;
import com.blps.app.domain.repository.AppUserRepository;
import com.blps.app.domain.repository.CourseRepository;
import com.blps.app.domain.repository.LearningTaskRepository;
import com.blps.app.domain.repository.TaskSubmissionRepository;
import com.blps.app.domain.repository.UserCourseProgressRepository;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Component("awardPointsDelegate")
public class AwardPointsDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(AwardPointsDelegate.class);

    private final AppUserRepository appUserRepository;
    private final CourseRepository courseRepository;
    private final LearningTaskRepository learningTaskRepository;
    private final TaskSubmissionRepository taskSubmissionRepository;
    private final UserCourseProgressRepository userCourseProgressRepository;

    public AwardPointsDelegate(AppUserRepository appUserRepository,
                               CourseRepository courseRepository,
                               LearningTaskRepository learningTaskRepository,
                               TaskSubmissionRepository taskSubmissionRepository,
                               UserCourseProgressRepository userCourseProgressRepository) {
        this.appUserRepository = appUserRepository;
        this.courseRepository = courseRepository;
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
        Object approvedVar = execution.getVariable("approved");
        boolean approved = approvedVar == null || Boolean.parseBoolean(approvedVar.toString());

        log.info("Executing award points: login={}, courseId={}, taskId={}, approved={}", login, courseId, taskId, approved);

        if (login == null || taskId == null) {
            return;
        }

        AppUser user = appUserRepository.findByLogin(login)
                .orElseThrow(() -> new BusinessException("User is not authenticated"));
        LearningTask task = learningTaskRepository.findByIdWithBlock(taskId)
                .orElseThrow(() -> new BusinessException("Task not found: " + taskId));
        Course course = courseId != null ? courseRepository.findById(courseId).orElse(task.getBlock().getCourse()) : task.getBlock().getCourse();

        UserCourseProgress progress = userCourseProgressRepository.findByUserAndCourse(user, course)
                .orElseGet(() -> userCourseProgressRepository.save(new UserCourseProgress(user, course)));

        int attempt = taskSubmissionRepository
                .findByUserAndTaskAndTask_Block_Course_IdOrderBySubmittedAtDesc(user, task, course.getId())
                .size() + 1;

        double coeff = getDoubleVariable(execution, "coefficient", 1.0);
        long calculatedPoints = Math.round(task.getBasePoints() * coeff);

        if (approved) {
            long previousAwarded = taskSubmissionRepository
                    .findFirstByUserAndTaskAndTask_Block_Course_IdAndStatusOrderBySubmittedAtDesc(
                            user, task, course.getId(), SubmissionStatus.APPROVED)
                    .map(TaskSubmission::getAwardedPoints)
                    .orElse(0L);

            long delta = Math.max(0L, calculatedPoints - previousAwarded);
            progress.addPoints(delta);

            Difficulty difficulty = Difficulty.EASY;
            Object diffVar = execution.getVariable("difficulty");
            if (diffVar != null) {
                try {
                    difficulty = Difficulty.valueOf(diffVar.toString().toUpperCase());
                } catch (Exception ignored) {}
            }

            Long mentorId = getLongVariable(execution, "mentorId");
            TaskSubmission submission = new TaskSubmission(
                    user,
                    task,
                    difficulty,
                    SubmissionStatus.APPROVED,
                    attempt,
                    calculatedPoints,
                    calculatedPoints,
                    mentorId,
                    OffsetDateTime.now()
            );
            TaskSubmission saved = taskSubmissionRepository.save(submission);

            execution.setVariable("submissionId", saved.getId());
            execution.setVariable("submissionStatus", saved.getStatus().name());
            execution.setVariable("awardedPoints", calculatedPoints);
            execution.setVariable("userPoints", progress.getPoints());

            // Check if course is completed
            long totalTasksInCourse = learningTaskRepository.countByBlock_Course_Id(course.getId());
            long approvedTasks = taskSubmissionRepository.countDistinctApprovedTasksByUserAndCourse(user, course.getId());
            boolean courseCompleted = totalTasksInCourse > 0 && approvedTasks >= totalTasksInCourse;
            execution.setVariable("courseCompleted", courseCompleted);

            log.info("Points awarded: {} (delta: {}). Total progress points: {}. Course completed: {}",
                    calculatedPoints, delta, progress.getPoints(), courseCompleted);
        } else {
            Long mentorId = getLongVariable(execution, "mentorId");
            Difficulty difficulty = Difficulty.EASY;
            TaskSubmission submission = new TaskSubmission(
                    user,
                    task,
                    difficulty,
                    SubmissionStatus.REJECTED,
                    attempt,
                    calculatedPoints,
                    0L,
                    mentorId,
                    OffsetDateTime.now()
            );
            TaskSubmission saved = taskSubmissionRepository.save(submission);
            execution.setVariable("submissionId", saved.getId());
            execution.setVariable("submissionStatus", saved.getStatus().name());
            execution.setVariable("awardedPoints", 0L);
            execution.setVariable("userPoints", progress.getPoints());
            log.info("Task submission rejected by reviewer for user: {}", login);
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

    private double getDoubleVariable(DelegateExecution execution, String name, double defaultValue) {
        Object val = execution.getVariable(name);
        if (val instanceof Number num) {
            return num.doubleValue();
        }
        if (val instanceof String str && !str.isBlank()) {
            return Double.parseDouble(str.trim());
        }
        return defaultValue;
    }
}
