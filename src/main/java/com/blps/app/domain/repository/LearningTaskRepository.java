package com.blps.app.domain.repository;

import com.blps.app.domain.model.LearningTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LearningTaskRepository extends JpaRepository<LearningTask, Long> {

	@Query("""
			select t
			from LearningTask t
			join fetch t.block b
			join fetch b.course c
			where c.id = :courseId
			""")
	List<LearningTask> findByCourseIdWithBlock(@Param("courseId") Long courseId);

	long countByBlock_Course_Id(Long courseId);

	Optional<LearningTask> findByCode(String code);
}
