package com.blps.app.application.bpmn.delegates;

import com.blps.app.domain.model.AppUser;
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
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("prepareCrmPaymentRequestDelegate")
public class PrepareCrmPaymentRequestDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(PrepareCrmPaymentRequestDelegate.class);

    private final AppUserRepository appUserRepository;
    private final CourseRepository courseRepository;
    private final CoursePurchaseRepository coursePurchaseRepository;
    private final CrmClient crmClient;

    public PrepareCrmPaymentRequestDelegate(AppUserRepository appUserRepository,
                                           CourseRepository courseRepository,
                                           CoursePurchaseRepository coursePurchaseRepository,
                                           CrmClient crmClient) {
        this.appUserRepository = appUserRepository;
        this.courseRepository = courseRepository;
        this.coursePurchaseRepository = coursePurchaseRepository;
        this.crmClient = crmClient;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String login = (String) execution.getVariable("login");
        if (login == null || login.isBlank()) {
            login = execution.getProcessBusinessKey();
            if (login != null && login.contains(":")) {
                login = login.substring(0, login.indexOf(":"));
            }
        }

        Object courseIdVar = execution.getVariable("courseId");
        Long courseId = 1L;
        if (courseIdVar instanceof Number num) {
            courseId = num.longValue();
        } else if (courseIdVar instanceof String str && !str.isBlank()) {
            try {
                courseId = Long.parseLong(str.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        log.info("Executing PrepareCrmPaymentRequestDelegate for user [{}] and courseId [{}]", login, courseId);

        AppUser user = login != null ? appUserRepository.findByLogin(login).orElse(null) : null;
        Course course = courseRepository.findById(courseId).orElse(null);

        if (user == null || course == null) {
            log.warn("User [{}] or Course [{}] not found when preparing CRM request", login, courseId);
            execution.setVariable("crmRequestPrepared", false);
            return;
        }

        execution.setVariable("login", login);
        execution.setVariable("courseId", course.getId());
        execution.setVariable("courseCode", course.getCode());
        execution.setVariable("courseTitle", course.getTitle());
        execution.setVariable("coursePrice", course.getPrice());
        execution.setVariable("amount", course.getPrice());

        // If already paid, mark flag
        if (coursePurchaseRepository.existsByUserAndCourseAndStatus(user, course, CoursePurchaseStatus.PAID)) {
            log.info("Course [{}] already purchased and PAID for user [{}]", course.getCode(), login);
            execution.setVariable("courseAlreadyPaid", true);
            execution.setVariable("paymentSuccess", true);
            execution.setVariable("crmRequestPrepared", true);
            return;
        }

        // Lookup existing purchase record to update if present
        CoursePurchase existingPurchase = coursePurchaseRepository.findByUserAndCourse(user, course).orElse(null);

        // Send invoice request to 1C CRM /courses/invoice
        try {
            log.info("Sending invoice request to 1C CRM (/courses/invoice): userLogin={}, courseCode={}, amount={}",
                    login, course.getCode(), course.getPrice());
            CrmCourseInvoiceDto invoice = crmClient.createCourseInvoice(
                    new CrmCourseInvoiceRequest(login, course.getCode(), course.getPrice())
            );
            log.info("Received invoice from 1C CRM: invoiceId={}, paymentUrl={}", invoice.invoiceId(), invoice.paymentUrl());

            CoursePurchase purchase = existingPurchase != null ? existingPurchase : new CoursePurchase(user, course, invoice.invoiceId(), course.getPrice());
            if (existingPurchase != null) {
                purchase.resetToPending(invoice.invoiceId(), course.getPrice());
            }
            coursePurchaseRepository.save(purchase);

            execution.setVariable("invoiceId", invoice.invoiceId());
            execution.setVariable("paymentUrl", invoice.paymentUrl());
            execution.setVariable("crmRequestPrepared", true);
        } catch (Exception e) {
            log.warn("1C CRM request failed: {}. Creating fallback pending invoice.", e.getMessage());
            String fallbackInvoiceId = UUID.randomUUID().toString();
            CoursePurchase purchase = existingPurchase != null ? existingPurchase : new CoursePurchase(user, course, fallbackInvoiceId, course.getPrice());
            if (existingPurchase != null) {
                purchase.resetToPending(fallbackInvoiceId, course.getPrice());
            }
            coursePurchaseRepository.save(purchase);

            execution.setVariable("invoiceId", fallbackInvoiceId);
            execution.setVariable("paymentUrl", "http://localhost/pay/" + fallbackInvoiceId);
            execution.setVariable("crmRequestPrepared", true);
        }
    }
}
