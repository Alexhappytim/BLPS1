package com.blps.app.domain.repo;

import com.blps.app.domain.entity.LearningTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningTaskRepository extends JpaRepository<LearningTask, Long> {
}
