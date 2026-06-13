package com.blps.app.infrastructure.crm;

import com.blps.app.infrastructure.crm.dto.*;

import java.util.List;

public interface CrmClient {
    CrmUserDto upsertUser(CrmUserUpsertRequest request);
    CrmCourseInvoiceDto createCourseInvoice(CrmCourseInvoiceRequest request);
    CrmMentorPayrollDto createMentorPayroll(CrmMentorPayrollRequest request);
}
