package com.blps.app.domain.repo;

import com.blps.app.domain.entity.AppUser;
import com.blps.app.domain.entity.LearningTask;
import com.blps.app.domain.entity.SubmissionStatus;
import com.blps.app.domain.entity.TaskSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskSubmissionRepository extends JpaRepository<TaskSubmission, Long> {

    List<TaskSubmission> findByUserAndTaskOrderBySubmittedAtDesc(AppUser user, LearningTask task);

    Optional<TaskSubmission> findFirstByUserAndTaskAndStatusOrderBySubmittedAtDesc(
            AppUser user,
            LearningTask task,
            SubmissionStatus status
    );
}
