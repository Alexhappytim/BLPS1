package com.blps.app.domain.repository;

import com.blps.app.domain.model.AppUser;
import com.blps.app.domain.model.Course;
import com.blps.app.domain.model.CoursePurchase;
import com.blps.app.domain.model.CoursePurchaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CoursePurchaseRepository extends JpaRepository<CoursePurchase, Long> {

    Optional<CoursePurchase> findByCrmInvoiceId(String crmInvoiceId);

    Optional<CoursePurchase> findByUserAndCourse(AppUser user, Course course);

    boolean existsByUserAndCourseAndStatus(AppUser user, Course course, CoursePurchaseStatus status);
}
