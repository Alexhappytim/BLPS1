package com.blps.app.infrastructure.crm;

import com.blps.app.infrastructure.crm.dto.*;
import com.blps.app.infrastructure.crm.jca.CrmConnection;
import com.blps.app.infrastructure.crm.jca.CrmConnectionFactory;
import jakarta.resource.ResourceException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SkillboxCrmClient implements CrmClient {

    private final CrmConnectionFactory connectionFactory;

    public SkillboxCrmClient(CrmConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public CrmUserDto upsertUser(CrmUserUpsertRequest request) {
        return execute(c -> c.upsertUser(request));
    }

    @Override
    public CrmCourseInvoiceDto createCourseInvoice(CrmCourseInvoiceRequest request) {
        return execute(c -> c.createCourseInvoice(request));
    }

    @Override
    public CrmMentorPayrollDto createMentorPayroll(CrmMentorPayrollRequest request) {
        return execute(c -> c.createMentorPayroll(request));
    }

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
