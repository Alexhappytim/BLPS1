package com.blps.app;

import com.blps.app.application.bpmn.delegates.GrantCourseAccessDelegate;
import com.blps.app.domain.model.AppUser;
import com.blps.app.domain.model.AppUserRole;
import com.blps.app.domain.model.Course;
import com.blps.app.domain.model.CoursePurchase;
import com.blps.app.domain.model.CoursePurchaseStatus;
import com.blps.app.domain.model.UserCourseProgress;
import com.blps.app.domain.repository.AppUserRepository;
import com.blps.app.domain.repository.CourseBlockRepository;
import com.blps.app.domain.repository.CoursePurchaseRepository;
import com.blps.app.domain.repository.CourseRepository;
import com.blps.app.domain.repository.UserCourseProgressRepository;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class GrantCourseAccessDelegateTest {

    private AppUserRepository appUserRepository;
    private CourseRepository courseRepository;
    private CourseBlockRepository courseBlockRepository;
    private CoursePurchaseRepository coursePurchaseRepository;
    private UserCourseProgressRepository userCourseProgressRepository;
    private GrantCourseAccessDelegate delegate;

    @BeforeEach
    void setUp() {
        appUserRepository = mock(AppUserRepository.class);
        courseRepository = mock(CourseRepository.class);
        courseBlockRepository = mock(CourseBlockRepository.class);
        coursePurchaseRepository = mock(CoursePurchaseRepository.class);
        userCourseProgressRepository = mock(UserCourseProgressRepository.class);
        delegate = new GrantCourseAccessDelegate(appUserRepository, courseRepository, courseBlockRepository, coursePurchaseRepository, userCourseProgressRepository);
    }

    @Test
    void testGrantCourseAccess_Success() throws Exception {
        AppUser user = new AppUser("student@test.local", "pwd", AppUserRole.USER, true, true);
        Course course = new Course("JAVA", "Java Developer", 25000L);
        CoursePurchase purchase = new CoursePurchase(user, course, "inv-12345", 25000L);

        when(appUserRepository.findByLogin("student@test.local")).thenReturn(Optional.of(user));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(coursePurchaseRepository.findByCrmInvoiceId("inv-12345")).thenReturn(Optional.of(purchase));
        when(userCourseProgressRepository.findByUserAndCourse(user, course)).thenReturn(Optional.empty());

        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("login")).thenReturn("student@test.local");
        when(execution.getVariable("courseId")).thenReturn(1L);
        when(execution.getVariable("invoiceId")).thenReturn("inv-12345");

        delegate.execute(execution);

        Assertions.assertEquals(CoursePurchaseStatus.PAID, purchase.getStatus());
        verify(coursePurchaseRepository).save(purchase);
        verify(userCourseProgressRepository).save(any(UserCourseProgress.class));
        verify(execution).setVariable("courseAccessGranted", true);
        verify(execution).setVariable("enrolled", true);
    }
}
