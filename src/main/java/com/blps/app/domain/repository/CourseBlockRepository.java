package com.blps.app.domain.repository;

import com.blps.app.domain.model.CourseBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseBlockRepository extends JpaRepository<CourseBlock, Long> {

	@Query("""
			select b
			from CourseBlock b
			join fetch b.course c
			where c.id = :courseId
			order by b.orderIndex asc
			""")
	List<CourseBlock> findByCourseIdWithCourse(@Param("courseId") Long courseId);

	Optional<CourseBlock> findByCode(String code);
}
