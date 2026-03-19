package com.blps.app.domain.service;

import com.blps.app.common.BusinessException;
import com.blps.app.domain.entity.AppUser;
import com.blps.app.domain.entity.CourseBlock;
import com.blps.app.domain.entity.Difficulty;
import com.blps.app.domain.entity.LearningTask;
import com.blps.app.domain.entity.ReviewType;
import com.blps.app.domain.entity.SubmissionStatus;
import com.blps.app.domain.entity.TaskSubmission;
import com.blps.app.domain.entity.UserBlockAccess;
import com.blps.app.domain.repo.AppUserRepository;
import com.blps.app.domain.repo.CourseBlockRepository;
import com.blps.app.domain.repo.LearningTaskRepository;
import com.blps.app.domain.repo.TaskSubmissionRepository;
import com.blps.app.domain.repo.UserBlockAccessRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class LearningPlatformService {

    private final AppUserRepository appUserRepository;
    private final CourseBlockRepository courseBlockRepository;
    private final LearningTaskRepository learningTaskRepository;
    private final TaskSubmissionRepository taskSubmissionRepository;
    private final UserBlockAccessRepository userBlockAccessRepository;

    public LearningPlatformService(AppUserRepository appUserRepository,
                                   CourseBlockRepository courseBlockRepository,
                                   LearningTaskRepository learningTaskRepository,
                                   TaskSubmissionRepository taskSubmissionRepository,
                                   UserBlockAccessRepository userBlockAccessRepository) {
        this.appUserRepository = appUserRepository;
        this.courseBlockRepository = courseBlockRepository;
        this.learningTaskRepository = learningTaskRepository;
        this.taskSubmissionRepository = taskSubmissionRepository;
        this.userBlockAccessRepository = userBlockAccessRepository;
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
    public SubmissionResult submitTask(String login, Long taskId, Difficulty difficulty, ReviewType reviewType) {
        AppUser user = requireUser(login);
        LearningTask task = learningTaskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("Task not found: " + taskId));

        if (task.isRequiresMentorReview() && reviewType != ReviewType.MENTOR) {
            throw new BusinessException("This task requires mentor review");
        }
        if (!task.isRequiresMentorReview() && reviewType == ReviewType.MENTOR) {
            throw new BusinessException("This task is checked automatically");
        }

        long calculatedPoints = Math.round(task.getBasePoints() * resolveCoefficient(difficulty));
        int attempt = taskSubmissionRepository.findByUserAndTaskOrderBySubmittedAtDesc(user, task).size() + 1;

        long previousAwarded = taskSubmissionRepository
                .findFirstByUserAndTaskAndStatusOrderBySubmittedAtDesc(user, task, SubmissionStatus.APPROVED)
                .map(TaskSubmission::getAwardedPoints)
                .orElse(0L);

        if (reviewType == ReviewType.AUTO) {
            long delta = calculatedPoints - previousAwarded;
            user.addPoints(delta);
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
            return new SubmissionResult(saved.getId(), saved.getStatus(), calculatedPoints, calculatedPoints, user.getPoints());
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
        return new SubmissionResult(saved.getId(), saved.getStatus(), calculatedPoints, null, user.getPoints());
    }

    @Transactional
    public SubmissionResult reviewSubmission(String login, Long submissionId, boolean approved) {
        requireUser(login);
        TaskSubmission submission = taskSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new BusinessException("Submission not found: " + submissionId));

        if (submission.getStatus() != SubmissionStatus.PENDING_REVIEW) {
            throw new BusinessException("Submission is not pending review");
        }

        AppUser user = submission.getUser();
        LearningTask task = submission.getTask();

        if (approved) {
            long previousAwarded = taskSubmissionRepository
                    .findFirstByUserAndTaskAndStatusOrderBySubmittedAtDesc(user, task, SubmissionStatus.APPROVED)
                    .map(TaskSubmission::getAwardedPoints)
                    .orElse(0L);
            long delta = submission.getCalculatedPoints() - previousAwarded;
            user.addPoints(delta);
            submission.approve(submission.getCalculatedPoints());
        } else {
            submission.reject();
        }

        return new SubmissionResult(
                submission.getId(),
                submission.getStatus(),
                submission.getCalculatedPoints(),
                submission.getAwardedPoints(),
                user.getPoints()
        );
    }

    @Transactional
    public BlockOpenResult openBlock(String login, Long blockId) {
        AppUser user = requireUser(login);
        CourseBlock block = courseBlockRepository.findById(blockId)
                .orElseThrow(() -> new BusinessException("Block not found: " + blockId));

        if (userBlockAccessRepository.existsByUserAndBlock(user, block)) {
            return new BlockOpenResult(blockId, true, user.getPoints());
        }

        if (user.getPoints() < block.getOpenCost()) {
            throw new BusinessException("Not enough points to open block");
        }

        user.subtractPoints(block.getOpenCost());
        userBlockAccessRepository.save(new UserBlockAccess(user, block));
        return new BlockOpenResult(blockId, false, user.getPoints());
    }

    @Transactional(readOnly = true)
    public long userPoints(String login) {
        return requireUser(login).getPoints();
    }

    @Transactional(readOnly = true)
    public List<CourseBlock> blocks() {
        return courseBlockRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<LearningTask> tasks() {
        return learningTaskRepository.findAll();
    }

    private AppUser requireUser(String login) {
        return appUserRepository.findByLogin(login)
                .orElseThrow(() -> new BusinessException("User is not authenticated"));
    }
}
