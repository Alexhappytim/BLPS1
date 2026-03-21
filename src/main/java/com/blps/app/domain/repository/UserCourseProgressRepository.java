package com.blps.app.domain.repository;

import com.blps.app.domain.model.AppUser;
import com.blps.app.domain.model.Course;
import com.blps.app.domain.model.UserCourseProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCourseProgressRepository extends JpaRepository<UserCourseProgress, Long> {

    Optional<UserCourseProgress> findByUserAndCourse(AppUser user, Course course);
}
