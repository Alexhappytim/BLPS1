package com.blps.app.domain.repo;

import com.blps.app.domain.entity.AppUser;
import com.blps.app.domain.entity.CourseBlock;
import com.blps.app.domain.entity.UserBlockAccess;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBlockAccessRepository extends JpaRepository<UserBlockAccess, Long> {

    boolean existsByUserAndBlock(AppUser user, CourseBlock block);
}
