package com.blps.app.application.service;

import com.blps.app.common.BusinessException;
import com.blps.app.domain.model.AppUser;
import com.blps.app.domain.model.Course;
import com.blps.app.domain.model.CourseBlock;
import com.blps.app.domain.model.Difficulty;
import com.blps.app.domain.model.LearningTask;
import com.blps.app.domain.model.ReviewType;
import com.blps.app.domain.model.SubmissionStatus;
import com.blps.app.domain.model.TaskSubmission;
import com.blps.app.domain.model.UserCourseProgress;
import com.blps.app.domain.model.UserBlockAccess;
import com.blps.app.domain.repository.AppUserRepository;
import com.blps.app.domain.repository.CourseRepository;
import com.blps.app.domain.repository.CourseBlockRepository;
import com.blps.app.domain.repository.LearningTaskRepository;
import com.blps.app.domain.repository.TaskSubmissionRepository;
import com.blps.app.domain.repository.UserCourseProgressRepository;
import com.blps.app.domain.repository.UserBlockAccessRepository;
import com.blps.app.infrastructure.notification.CourseCertificateSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class LearningPlatformService {

    private final AppUserRepository appUserRepository;
    private final CourseRepository courseRepository;
    private final CourseBlockRepository courseBlockRepository;
    private final LearningTaskRepository learningTaskRepository;
    private final TaskSubmissionRepository taskSubmissionRepository;
    private final UserCourseProgressRepository userCourseProgressRepository;
    private final UserBlockAccessRepository userBlockAccessRepository;
    private final CourseCertificateSender courseCertificateSender;

    public LearningPlatformService(AppUserRepository appUserRepository,
                                   CourseRepository courseRepository,
                                   CourseBlockRepository courseBlockRepository,
                                   LearningTaskRepository learningTaskRepository,
                                   TaskSubmissionRepository taskSubmissionRepository,
                                   UserCourseProgressRepository userCourseProgressRepository,
                                   UserBlockAccessRepository userBlockAccessRepository,
                                   CourseCertificateSender courseCertificateSender) {
        this.appUserRepository = appUserRepository;
        this.courseRepository = courseRepository;
        this.courseBlockRepository = courseBlockRepository;
        this.learningTaskRepository = learningTaskRepository;
        this.taskSubmissionRepository = taskSubmissionRepository;
        this.userCourseProgressRepository = userCourseProgressRepository;
        this.userBlockAccessRepository = userBlockAccessRepository;
        this.courseCertificateSender = courseCertificateSender;
    }

    @Transactional(readOnly = true)
    public double resolveCoefficient(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 1.0;
            case MEDIUM -> 1.5;
            case HARD -> 2.0;
        };
    }

    @Transactional
    public SubmissionResult submitTask(String login, Long courseId, Long taskId, Difficulty difficulty, ReviewType reviewType) {
        AppUser user = requireUser(login);
        Course course = requireCourse(courseId);
        LearningTask task = learningTaskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("Task not found: " + taskId));

        ensureTaskBelongsToCourse(task, courseId);
        
        if (!userBlockAccessRepository.existsByUserAndBlock(user, task.getBlock())) {
            throw new BusinessException("Block is not opened. Please open the block first");
        }
        
        UserCourseProgress progress = getOrCreateProgress(user, course);

        if (task.isRequiresMentorReview() && reviewType != ReviewType.MENTOR) {
            throw new BusinessException("This task requires mentor review");
        }
        if (!task.isRequiresMentorReview() && reviewType == ReviewType.MENTOR) {
            throw new BusinessException("This task is checked automatically");
        }

        long calculatedPoints = Math.round(task.getBasePoints() * resolveCoefficient(difficulty));
        int attempt = taskSubmissionRepository
            .findByUserAndTaskAndTask_Block_Course_IdOrderBySubmittedAtDesc(user, task, courseId)
            .size() + 1;

        long previousAwarded = taskSubmissionRepository
            .findFirstByUserAndTaskAndTask_Block_Course_IdAndStatusOrderBySubmittedAtDesc(
                user,
                task,
                courseId,
                SubmissionStatus.APPROVED
            )
                .map(TaskSubmission::getAwardedPoints)
                .orElse(0L);

        if (reviewType == ReviewType.AUTO) {
            long delta = calculatedPoints - previousAwarded;
                progress.addPoints(delta);
            TaskSubmission submission = new TaskSubmission(
                    user,
                    task,
                    difficulty,
                    SubmissionStatus.APPROVED,
                    attempt,
                    calculatedPoints,
                    calculatedPoints,
                    OffsetDateTime.now()
            );
            TaskSubmission saved = taskSubmissionRepository.save(submission);
                trySendCourseCertificate(user, course, progress);
                return new SubmissionResult(
                    saved.getId(),
                    saved.getStatus(),
                    calculatedPoints,
                    calculatedPoints,
                    progress.getPoints()
                );
        }

        TaskSubmission pending = new TaskSubmission(
                user,
                task,
                difficulty,
                SubmissionStatus.PENDING_REVIEW,
                attempt,
                calculatedPoints,
                null,
                OffsetDateTime.now()
        );
        TaskSubmission saved = taskSubmissionRepository.save(pending);
        return new SubmissionResult(saved.getId(), saved.getStatus(), calculatedPoints, null, progress.getPoints());
    }

    @Transactional
    public SubmissionResult reviewSubmission(String login, Long courseId, Long submissionId, boolean approved) {
        requireUser(login);
        Course course = requireCourse(courseId);
        TaskSubmission submission = taskSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new BusinessException("Submission not found: " + submissionId));

        ensureTaskBelongsToCourse(submission.getTask(), courseId);

        if (submission.getStatus() != SubmissionStatus.PENDING_REVIEW) {
            throw new BusinessException("Submission is not pending review");
        }

        AppUser user = submission.getUser();
        LearningTask task = submission.getTask();
        UserCourseProgress progress = getOrCreateProgress(user, course);

        if (approved) {
            long previousAwarded = taskSubmissionRepository
                    .findFirstByUserAndTaskAndTask_Block_Course_IdAndStatusOrderBySubmittedAtDesc(
                            user,
                            task,
                            courseId,
                            SubmissionStatus.APPROVED
                    )
                    .map(TaskSubmission::getAwardedPoints)
                    .orElse(0L);
            long delta = submission.getCalculatedPoints() - previousAwarded;
            progress.addPoints(delta);
            submission.approve(submission.getCalculatedPoints());
            trySendCourseCertificate(user, course, progress);
        } else {
            submission.reject();
        }

        return new SubmissionResult(
                submission.getId(),
                submission.getStatus(),
                submission.getCalculatedPoints(),
                submission.getAwardedPoints(),
                progress.getPoints()
        );
    }

    @Transactional
    public BlockOpenResult openBlock(String login, Long courseId, Long blockId) {
        AppUser user = requireUser(login);
        Course course = requireCourse(courseId);
        CourseBlock block = courseBlockRepository.findById(blockId)
                .orElseThrow(() -> new BusinessException("Block not found: " + blockId));

        ensureBlockBelongsToCourse(block, courseId);
        UserCourseProgress progress = getOrCreateProgress(user, course);

        if (userBlockAccessRepository.existsByUserAndBlock(user, block)) {
            return new BlockOpenResult(blockId, true, progress.getPoints());
        }

        if (progress.getPoints() < block.getOpenCost()) {
            throw new BusinessException("Not enough points to open block");
        }

        progress.subtractPoints(block.getOpenCost());
        userBlockAccessRepository.save(new UserBlockAccess(user, block));
        return new BlockOpenResult(blockId, false, progress.getPoints());
    }

    @Transactional(readOnly = true)
    public long userPoints(String login, Long courseId) {
        AppUser user = requireUser(login);
        Course course = requireCourse(courseId);
        return getOrCreateProgress(user, course).getPoints();
    }

    @Transactional(readOnly = true)
    public List<Course> courses() {
        return courseRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<CourseBlock> blocks(Long courseId) {
        requireCourse(courseId);
        return courseBlockRepository.findByCourseIdWithCourse(courseId);
    }

    @Transactional(readOnly = true)
    public List<LearningTask> tasks(Long courseId) {
        requireCourse(courseId);
        return learningTaskRepository.findByCourseIdWithBlock(courseId);
    }

    private AppUser requireUser(String login) {
        return appUserRepository.findByLogin(login)
                .orElseThrow(() -> new BusinessException("User is not authenticated"));
    }

    private Course requireCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException("Course not found: " + courseId));
    }

    private UserCourseProgress getOrCreateProgress(AppUser user, Course course) {
        return userCourseProgressRepository.findByUserAndCourse(user, course)
                .orElseGet(() -> userCourseProgressRepository.save(new UserCourseProgress(user, course)));
    }

    private void ensureBlockBelongsToCourse(CourseBlock block, Long courseId) {
        if (!block.getCourse().getId().equals(courseId)) {
            throw new BusinessException("Block does not belong to selected course");
        }
    }

    private void ensureTaskBelongsToCourse(LearningTask task, Long courseId) {
        if (!task.getBlock().getCourse().getId().equals(courseId)) {
            throw new BusinessException("Task does not belong to selected course");
        }
    }

    private void trySendCourseCertificate(AppUser user, Course course, UserCourseProgress progress) {
        if (progress.isCertificateSent()) {
            return;
        }

        long totalTasksInCourse = learningTaskRepository.countByBlock_Course_Id(course.getId());
        if (totalTasksInCourse == 0) {
            return;
        }

        long approvedTasks = taskSubmissionRepository.countDistinctApprovedTasksByUserAndCourse(user, course.getId());
        if (approvedTasks < totalTasksInCourse) {
            return;
        }

        boolean sent = courseCertificateSender.sendCourseCompletionCertificate(user, course);
        if (sent) {
            progress.markCertificateSent();
        }
    }
}
