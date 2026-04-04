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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class LearningPlatformService {

    private static final int PAGE_SIZE = 10;

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
    public SubmissionResult submitTask(String login, Long courseId, Long taskId, Difficulty difficulty) {
        AppUser user = requireUser(login);
        Course course = requireCourse(courseId);
        LearningTask task = requireTask(taskId);
        ReviewType reviewType = task.getReviewType();

        ensureTaskBelongsToCourse(task, courseId);

        if (!userBlockAccessRepository.existsByUserAndBlock(user, task.getBlock())) {
            throw new BusinessException("Block is not opened. Please open the block first");
        }

        UserCourseProgress progress = getOrCreateProgress(user, course);

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
        CourseBlock block = requireBlock(blockId);

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
        return userCourseProgressRepository.findByUserAndCourse(user, course)
                .map(UserCourseProgress::getPoints)
                .orElse(0L);
    }

    @Transactional(readOnly = true)
    public Page<Course> courses(int page) {
        return courseRepository.findAll(pageRequest(page));
    }

    @Transactional(readOnly = true)
    public Course courseById(Long id) {
        return requireCourse(id);
    }

    @Transactional
    public Course createCourse(String code, String title) {
        ensureCourseCodeUnique(code, null);
        return courseRepository.save(new Course(code, title));
    }

    @Transactional
    public Course updateCourse(Long id, String code, String title) {
        Course course = requireCourse(id);
        ensureCourseCodeUnique(code, id);
        course.update(code, title);
        return course;
    }

    @Transactional
    public void deleteCourse(Long id) {
        Course course = requireCourse(id);
        if (courseBlockRepository.existsByCourse_Id(id) || userCourseProgressRepository.existsByCourse_Id(id)) {
            throw new BusinessException("Cannot delete course with related blocks or user progress");
        }
        courseRepository.delete(course);
    }

    @Transactional(readOnly = true)
    public Page<CourseBlock> blocks(Long courseId, int page) {
        requireCourse(courseId);
        return courseBlockRepository.findPageByCourseIdWithCourse(courseId, pageRequest(page));
    }

    @Transactional(readOnly = true)
    public CourseBlock blockById(Long id) {
        return requireBlock(id);
    }

    @Transactional
    public CourseBlock createBlock(Long courseId, String code, String title, long openCost, int orderIndex) {
        Course course = requireCourse(courseId);
        ensureBlockCodeUnique(code, null);
        return courseBlockRepository.save(new CourseBlock(code, title, openCost, orderIndex, course));
    }

    @Transactional
    public CourseBlock updateBlock(Long id, Long courseId, String code, String title, long openCost, int orderIndex) {
        CourseBlock block = requireBlock(id);
        Course course = requireCourse(courseId);
        ensureBlockCodeUnique(code, id);
        block.update(code, title, openCost, orderIndex, course);
        return block;
    }

    @Transactional
    public void deleteBlock(Long id) {
        CourseBlock block = requireBlock(id);
        if (learningTaskRepository.existsByBlock_Id(id) || userBlockAccessRepository.existsByBlock_Id(id)) {
            throw new BusinessException("Cannot delete block with related tasks or access records");
        }
        courseBlockRepository.delete(block);
    }

    @Transactional(readOnly = true)
    public Page<LearningTask> tasks(Long courseId, int page) {
        requireCourse(courseId);
        return learningTaskRepository.findPageByCourseIdWithBlock(courseId, pageRequest(page));
    }

    @Transactional(readOnly = true)
    public LearningTask taskById(Long id) {
        return requireTask(id);
    }

    @Transactional
    public LearningTask createTask(Long blockId, String code, String title, long basePoints, ReviewType reviewType) {
        CourseBlock block = requireBlock(blockId);
        ensureTaskCodeUnique(code, null);
        return learningTaskRepository.save(new LearningTask(code, title, basePoints, reviewType, block));
    }

    @Transactional
    public LearningTask updateTask(Long id, Long blockId, String code, String title, long basePoints, ReviewType reviewType) {
        LearningTask task = requireTask(id);
        CourseBlock block = requireBlock(blockId);
        ensureTaskCodeUnique(code, id);
        task.update(code, title, basePoints, reviewType, block);
        return task;
    }

    @Transactional
    public void deleteTask(Long id) {
        LearningTask task = requireTask(id);
        if (taskSubmissionRepository.existsByTask_Id(id)) {
            throw new BusinessException("Cannot delete task with existing submissions");
        }
        learningTaskRepository.delete(task);
    }

    private AppUser requireUser(String login) {
        return appUserRepository.findByLogin(login)
                .orElseThrow(() -> new BusinessException("User is not authenticated"));
    }

    private Course requireCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException("Course not found: " + courseId));
    }

    private CourseBlock requireBlock(Long blockId) {
        return courseBlockRepository.findByIdWithCourse(blockId)
                .orElseThrow(() -> new BusinessException("Block not found: " + blockId));
    }

    private LearningTask requireTask(Long taskId) {
        return learningTaskRepository.findByIdWithBlock(taskId)
                .orElseThrow(() -> new BusinessException("Task not found: " + taskId));
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

    private void ensureCourseCodeUnique(String code, Long excludeId) {
        boolean duplicated = excludeId == null
                ? courseRepository.findByCode(code).isPresent()
                : courseRepository.existsByCodeAndIdNot(code, excludeId);
        if (duplicated) {
            throw new BusinessException("Course code already exists: " + code);
        }
    }

    private void ensureBlockCodeUnique(String code, Long excludeId) {
        var existing = courseBlockRepository.findByCode(code);
        if (existing.isPresent() && (excludeId == null || !existing.get().getId().equals(excludeId))) {
            throw new BusinessException("Block code already exists: " + code);
        }
    }

    private void ensureTaskCodeUnique(String code, Long excludeId) {
        var existing = learningTaskRepository.findByCode(code);
        if (existing.isPresent() && (excludeId == null || !existing.get().getId().equals(excludeId))) {
            throw new BusinessException("Task code already exists: " + code);
        }
    }

    private Pageable pageRequest(int page) {
        if (page < 0) {
            throw new BusinessException("Page index must be >= 0");
        }
        return PageRequest.of(page, PAGE_SIZE);
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
