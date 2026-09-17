package com.blps.app;

import com.blps.app.application.bpmn.delegates.PrepareCrmPaymentRequestDelegate;
import com.blps.app.domain.model.AppUser;
import com.blps.app.domain.model.AppUserRole;
import com.blps.app.domain.model.Course;
import com.blps.app.domain.model.CoursePurchase;
import com.blps.app.domain.model.CoursePurchaseStatus;
import com.blps.app.domain.repository.AppUserRepository;
import com.blps.app.domain.repository.CoursePurchaseRepository;
import com.blps.app.domain.repository.CourseRepository;
import com.blps.app.infrastructure.crm.CrmClient;
import com.blps.app.infrastructure.crm.dto.CrmCourseInvoiceDto;
import com.blps.app.infrastructure.crm.dto.CrmCourseInvoiceRequest;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PrepareCrmPaymentRequestDelegateTest {

    private AppUserRepository appUserRepository;
    private CourseRepository courseRepository;
    private CoursePurchaseRepository coursePurchaseRepository;
    private CrmClient crmClient;
    private PrepareCrmPaymentRequestDelegate delegate;

    @BeforeEach
    void setUp() {
        appUserRepository = mock(AppUserRepository.class);
        courseRepository = mock(CourseRepository.class);
        coursePurchaseRepository = mock(CoursePurchaseRepository.class);
        crmClient = mock(CrmClient.class);
        delegate = new PrepareCrmPaymentRequestDelegate(appUserRepository, courseRepository, coursePurchaseRepository, crmClient);
    }

    @Test
    void testPrepareCrmPaymentRequest_Success() throws Exception {
        AppUser user = new AppUser("student@test.local", "pwd", AppUserRole.USER, true, true);
        Course course = new Course("JAVA", "Java Developer", 25000L);

        when(appUserRepository.findByLogin("student@test.local")).thenReturn(Optional.of(user));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(coursePurchaseRepository.existsByUserAndCourseAndStatus(user, course, CoursePurchaseStatus.PAID)).thenReturn(false);
        when(coursePurchaseRepository.findByUserAndCourse(user, course)).thenReturn(Optional.empty());

        CrmCourseInvoiceDto invoiceDto = new CrmCourseInvoiceDto(
                "inv-12345",
                "student@test.local",
                "JAVA",
                25000L,
                "PENDING_PAYMENT",
                "http://localhost/pay/inv-12345",
                OffsetDateTime.now()
        );
        when(crmClient.createCourseInvoice(any(CrmCourseInvoiceRequest.class))).thenReturn(invoiceDto);

        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("login")).thenReturn("student@test.local");
        when(execution.getVariable("courseId")).thenReturn(1L);

        delegate.execute(execution);

        ArgumentCaptor<CrmCourseInvoiceRequest> reqCaptor = ArgumentCaptor.forClass(CrmCourseInvoiceRequest.class);
        verify(crmClient).createCourseInvoice(reqCaptor.capture());
        Assertions.assertEquals("student@test.local", reqCaptor.getValue().userLogin());
        Assertions.assertEquals("JAVA", reqCaptor.getValue().courseCode());
        Assertions.assertEquals(25000L, reqCaptor.getValue().amount());

        verify(coursePurchaseRepository).save(any(CoursePurchase.class));
        verify(execution).setVariable("invoiceId", "inv-12345");
        verify(execution).setVariable("paymentUrl", "http://localhost/pay/inv-12345");
        verify(execution).setVariable("crmRequestPrepared", true);
    }
}
