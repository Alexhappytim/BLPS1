package com.blps.app.infrastructure.crm;

import com.blps.app.infrastructure.crm.dto.*;
import com.blps.app.infrastructure.crm.jca.CrmConnection;
import com.blps.app.infrastructure.crm.jca.CrmConnectionFactory;
import jakarta.resource.ResourceException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adapter implementing {@link CrmClient} by obtaining a JCA {@link CrmConnection}
 * from the {@link CrmConnectionFactory} for each operation.
 *
 * Each method opens a connection, performs the call, then closes it.
 * The underlying JAX-RS client is shared and thread-safe.
 */
@Component
public class SkillboxCrmClient implements CrmClient {

    private final CrmConnectionFactory connectionFactory;

    public SkillboxCrmClient(CrmConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public List<CrmUserDto> getUsers() {
        return execute(CrmConnection::getUsers);
    }

    @Override
    public CrmUserDto upsertUser(CrmUserUpsertRequest request) {
        return execute(c -> c.upsertUser(request));
    }

    @Override
    public List<CrmMentorDto> getMentors() {
        return execute(CrmConnection::getMentors);
    }

    @Override
    public CrmMentorDto upsertMentor(CrmMentorUpsertRequest request) {
        return execute(c -> c.upsertMentor(request));
    }

    @Override
    public List<CrmCourseDto> getCourses() {
        return execute(CrmConnection::getCourses);
    }

    @Override
    public CrmCourseDto upsertCourse(CrmCourseUpsertRequest request) {
        return execute(c -> c.upsertCourse(request));
    }

    @Override
    public List<CrmTaskDto> getTasks() {
        return execute(CrmConnection::getTasks);
    }

    @Override
    public CrmTaskDto upsertTask(CrmTaskUpsertRequest request) {
        return execute(c -> c.upsertTask(request));
    }

    @Override
    public List<CrmTaskReviewDto> getTaskReviews() {
        return execute(CrmConnection::getTaskReviews);
    }

    @Override
    public CrmTaskReviewDto upsertTaskReview(CrmTaskReviewUpsertRequest request) {
        return execute(c -> c.upsertTaskReview(request));
    }

    @Override
    public List<CrmCourseInvoiceDto> getCourseInvoices() {
        return execute(CrmConnection::getCourseInvoices);
    }

    @Override
    public CrmCourseInvoiceDto createCourseInvoice(CrmCourseInvoiceRequest request) {
        return execute(c -> c.createCourseInvoice(request));
    }

    @Override
    public List<CrmMentorPayrollDto> getMentorPayroll() {
        return execute(CrmConnection::getMentorPayroll);
    }

    @Override
    public CrmMentorPayrollDto createMentorPayroll(CrmMentorPayrollRequest request) {
        return execute(c -> c.createMentorPayroll(request));
    }

    // ── JCA execution helper ──────────────────────────────────────────────────

    /**
     * Opens a JCA connection, applies the given operation, then closes the handle.
     * Both {@link ResourceException} (from open/close) and any runtime exception
     * (from the operation itself) are propagated as {@link RuntimeException}.
     */
    private <T> T execute(CrmOperation<T> operation) {
        CrmConnection connection = null;
        try {
            connection = connectionFactory.getConnection();
            return operation.apply(connection);
        } catch (ResourceException e) {
            throw new RuntimeException("JCA CRM connection error", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (ResourceException e) {
                    // Log and swallow — close errors should not mask the original result
                    throw new RuntimeException("Failed to close JCA CRM connection", e);
                }
            }
        }
    }

    @FunctionalInterface
    private interface CrmOperation<T> {
        T apply(CrmConnection connection) throws ResourceException;
    }
}
