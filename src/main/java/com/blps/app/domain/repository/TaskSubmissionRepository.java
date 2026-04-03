package com.blps.app.domain.repository;

import com.blps.app.domain.model.AppUser;
import com.blps.app.domain.model.LearningTask;
import com.blps.app.domain.model.SubmissionStatus;
import com.blps.app.domain.model.TaskSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskSubmissionRepository extends JpaRepository<TaskSubmission, Long> {

    List<TaskSubmission> findByUserAndTaskOrderBySubmittedAtDesc(AppUser user, LearningTask task);

    Optional<TaskSubmission> findFirstByUserAndTaskAndStatusOrderBySubmittedAtDesc(
            AppUser user,
            LearningTask task,
            SubmissionStatus status
    );

        List<TaskSubmission> findByUserAndTaskAndTask_Block_Course_IdOrderBySubmittedAtDesc(
            AppUser user,
            LearningTask task,
            Long courseId
        );

        Optional<TaskSubmission> findFirstByUserAndTaskAndTask_Block_Course_IdAndStatusOrderBySubmittedAtDesc(
            AppUser user,
            LearningTask task,
            Long courseId,
            SubmissionStatus status
        );

        @Query("""
                        select count(distinct s.task.id)
                        from TaskSubmission s
                        where s.user = :user
                            and s.status = com.blps.app.domain.model.SubmissionStatus.APPROVED
                            and s.task.block.course.id = :courseId
                        """)
        long countDistinctApprovedTasksByUserAndCourse(@Param("user") AppUser user, @Param("courseId") Long courseId);

        boolean existsByTask_Id(Long taskId);
}
