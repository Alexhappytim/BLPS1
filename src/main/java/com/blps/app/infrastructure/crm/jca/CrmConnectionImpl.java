package com.blps.app.infrastructure.crm.jca;

import com.blps.app.common.BusinessException;
import com.blps.app.infrastructure.crm.dto.*;
import jakarta.resource.ResourceException;
import jakarta.resource.cci.*;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

/**
 * JCA Connection implementation.
 * This is the logical handle returned to service code.
 * All actual HTTP REST calls to the CRM are made here.
 *
 * The required {@link jakarta.resource.cci.Connection} SPI methods are implemented
 * as no-ops / stubs since this adapter does not support CCI interactions or transactions.
 */
public class CrmConnectionImpl implements CrmConnection {

    private final Client client;
    private final String baseUrl;
    private boolean closed = false;

    public CrmConnectionImpl(Client client, String baseUrl) {
        this.client = client;
        this.baseUrl = baseUrl;
    }

    // ── business methods ─────────────────────────────────────────────────────

    @Override
    public List<CrmUserDto> getUsers() {
        ensureOpen();
        return getList("users", new GenericType<>() {});
    }

    @Override
    public CrmUserDto upsertUser(CrmUserUpsertRequest request) {
        ensureOpen();
        return post("users", request, CrmUserDto.class);
    }

    @Override
    public List<CrmMentorDto> getMentors() {
        ensureOpen();
        return getList("mentors", new GenericType<>() {});
    }

    @Override
    public CrmMentorDto upsertMentor(CrmMentorUpsertRequest request) {
        ensureOpen();
        return post("mentors", request, CrmMentorDto.class);
    }

    @Override
    public List<CrmCourseDto> getCourses() {
        ensureOpen();
        return getList("courses", new GenericType<>() {});
    }

    @Override
    public CrmCourseDto upsertCourse(CrmCourseUpsertRequest request) {
        ensureOpen();
        return post("courses", request, CrmCourseDto.class);
    }

    @Override
    public List<CrmTaskDto> getTasks() {
        ensureOpen();
        return getList("tasks", new GenericType<>() {});
    }

    @Override
    public CrmTaskDto upsertTask(CrmTaskUpsertRequest request) {
        ensureOpen();
        return post("tasks", request, CrmTaskDto.class);
    }

    @Override
    public List<CrmTaskReviewDto> getTaskReviews() {
        ensureOpen();
        return getList("task-reviews", new GenericType<>() {});
    }

    @Override
    public CrmTaskReviewDto upsertTaskReview(CrmTaskReviewUpsertRequest request) {
        ensureOpen();
        return post("task-reviews", request, CrmTaskReviewDto.class);
    }

    @Override
    public List<CrmCourseInvoiceDto> getCourseInvoices() {
        ensureOpen();
        return getList("courses/invoice", new GenericType<>() {});
    }

    @Override
    public CrmCourseInvoiceDto createCourseInvoice(CrmCourseInvoiceRequest request) {
        ensureOpen();
        return post("courses/invoice", request, CrmCourseInvoiceDto.class);
    }

    @Override
    public List<CrmMentorPayrollDto> getMentorPayroll() {
        ensureOpen();
        return getList("mentor/payroll", new GenericType<>() {});
    }

    @Override
    public CrmMentorPayrollDto createMentorPayroll(CrmMentorPayrollRequest request) {
        ensureOpen();
        return post("mentor/payroll", request, CrmMentorPayrollDto.class);
    }

    // ── jakarta.resource.cci.Connection SPI stubs ─────────────────────────────

    @Override
    public Interaction createInteraction() throws ResourceException {
        throw new ResourceException("CCI Interaction not supported by this adapter");
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        // No local transaction support for a REST endpoint
        return null;
    }

    @Override
    public ConnectionMetaData getMetaData() throws ResourceException {
        return new ConnectionMetaData() {
            @Override public String getEISProductName() { return "Skillbox CRM"; }
            @Override public String getEISProductVersion() { return "1.0"; }
            @Override public String getUserName() { return ""; }
        };
    }

    @Override
    public ResultSetInfo getResultSetInfo() throws ResourceException {
        throw new ResourceException("ResultSetInfo not supported by this adapter");
    }

    @Override
    public void close() throws ResourceException {
        this.closed = true;
        // The underlying JAX-RS Client is shared and managed by Spring —
        // do NOT close it here, only mark this logical handle as invalid.
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("CRM connection handle has already been closed");
        }
    }

    private <T> List<T> getList(String path, GenericType<List<T>> type) {
        try (Response response = client
                .target(baseUrl)
                .path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get()) {
            ensureSuccess(response, "GET", path);
            return response.readEntity(type);
        }
    }

    private <REQ, RES> RES post(String path, REQ requestBody, Class<RES> responseClass) {
        try (Response response = client
                .target(baseUrl)
                .path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(requestBody, MediaType.APPLICATION_JSON_TYPE))) {
            ensureSuccess(response, "POST", path);
            return response.readEntity(responseClass);
        }
    }

    private void ensureSuccess(Response response, String method, String path) {
        int status = response.getStatus();
        if (status >= 200 && status < 300) {
            return;
        }
        String body;
        try {
            body = response.readEntity(String.class);
        } catch (Exception ignored) {
            body = "<unavailable>";
        }
        throw new BusinessException(
                "CRM call failed: " + method + " " + baseUrl + "/" + path
                + " -> " + status + "; body=" + body);
    }
}
