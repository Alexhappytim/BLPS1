package com.blps.app.infrastructure.crm.jca;

import com.blps.app.infrastructure.crm.dto.*;
import jakarta.resource.ResourceException;
import jakarta.resource.cci.Connection;
import jakarta.resource.cci.ConnectionMetaData;
import jakarta.resource.cci.Interaction;
import jakarta.resource.cci.LocalTransaction;
import jakarta.resource.cci.ResultSetInfo;

import java.util.List;

/**
 * JCA Connection handle exposing CRM business operations.
 * Extends {@link jakarta.resource.cci.Connection} so the covariant return type
 * in {@link CrmConnectionFactory#getConnection()} is satisfied by the JCA SPI.
 * Use in try-with-resources to ensure proper cleanup.
 */
public interface CrmConnection extends Connection {

    // ── business methods ─────────────────────────────────────────────────────

    List<CrmUserDto> getUsers();
    CrmUserDto upsertUser(CrmUserUpsertRequest request);

    List<CrmMentorDto> getMentors();
    CrmMentorDto upsertMentor(CrmMentorUpsertRequest request);

    List<CrmCourseDto> getCourses();
    CrmCourseDto upsertCourse(CrmCourseUpsertRequest request);

    List<CrmTaskDto> getTasks();
    CrmTaskDto upsertTask(CrmTaskUpsertRequest request);

    List<CrmTaskReviewDto> getTaskReviews();
    CrmTaskReviewDto upsertTaskReview(CrmTaskReviewUpsertRequest request);

    List<CrmCourseInvoiceDto> getCourseInvoices();
    CrmCourseInvoiceDto createCourseInvoice(CrmCourseInvoiceRequest request);

    List<CrmMentorPayrollDto> getMentorPayroll();
    CrmMentorPayrollDto createMentorPayroll(CrmMentorPayrollRequest request);
}
