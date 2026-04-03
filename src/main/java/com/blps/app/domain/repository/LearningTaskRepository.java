package com.blps.app.domain.repository;

import com.blps.app.domain.model.LearningTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

	@Query(value = """
			select t
			from LearningTask t
			join fetch t.block b
			join fetch b.course c
			where c.id = :courseId
			""",
			countQuery = """
			select count(t)
			from LearningTask t
			where t.block.course.id = :courseId
			""")
	Page<LearningTask> findPageByCourseIdWithBlock(@Param("courseId") Long courseId, Pageable pageable);

	@Query("""
			select t
			from LearningTask t
			join fetch t.block b
			join fetch b.course
			where t.id = :id
			""")
	Optional<LearningTask> findByIdWithBlock(@Param("id") Long id);

	long countByBlock_Course_Id(Long courseId);

	Optional<LearningTask> findByCode(String code);

	boolean existsByBlock_Id(Long blockId);
}
