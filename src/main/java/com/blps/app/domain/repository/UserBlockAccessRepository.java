package com.blps.app.domain.repository;

import com.blps.app.domain.model.AppUser;
import com.blps.app.domain.model.CourseBlock;
import com.blps.app.domain.model.UserBlockAccess;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBlockAccessRepository extends JpaRepository<UserBlockAccess, Long> {

    boolean existsByUserAndBlock(AppUser user, CourseBlock block);
}
