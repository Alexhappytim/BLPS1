package com.blps.app.infrastructure.crm;

import com.blps.app.infrastructure.crm.dto.*;

import java.util.List;

public interface CrmClient {

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
