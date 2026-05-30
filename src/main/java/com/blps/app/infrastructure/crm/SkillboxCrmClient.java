package com.blps.app.infrastructure.crm;

import com.blps.app.common.BusinessException;
import com.blps.app.infrastructure.crm.dto.*;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SkillboxCrmClient implements CrmClient {

    private final Client client;
    private final String baseUrl;

    public SkillboxCrmClient(Client crmJaxRsClient, String crmBaseUrl) {
        this.client = crmJaxRsClient;
        this.baseUrl = crmBaseUrl;
    }

    @Override
    public List<CrmUserDto> getUsers() {
        return getList("users", new GenericType<>() {});
    }

    @Override
    public CrmUserDto upsertUser(CrmUserUpsertRequest request) {
        return post("users", request, CrmUserDto.class);
    }

    @Override
    public List<CrmMentorDto> getMentors() {
        return getList("mentors", new GenericType<>() {});
    }

    @Override
    public CrmMentorDto upsertMentor(CrmMentorUpsertRequest request) {
        return post("mentors", request, CrmMentorDto.class);
    }

    @Override
    public List<CrmCourseDto> getCourses() {
        return getList("courses", new GenericType<>() {});
    }

    @Override
    public CrmCourseDto upsertCourse(CrmCourseUpsertRequest request) {
        return post("courses", request, CrmCourseDto.class);
    }

    @Override
    public List<CrmTaskDto> getTasks() {
        return getList("tasks", new GenericType<>() {});
    }

    @Override
    public CrmTaskDto upsertTask(CrmTaskUpsertRequest request) {
        return post("tasks", request, CrmTaskDto.class);
    }

    @Override
    public List<CrmTaskReviewDto> getTaskReviews() {
        return getList("task-reviews", new GenericType<>() {});
    }

    @Override
    public CrmTaskReviewDto upsertTaskReview(CrmTaskReviewUpsertRequest request) {
        return post("task-reviews", request, CrmTaskReviewDto.class);
    }

    @Override
    public List<CrmCourseInvoiceDto> getCourseInvoices() {
        return getList("courses/invoice", new GenericType<>() {});
    }

    @Override
    public CrmCourseInvoiceDto createCourseInvoice(CrmCourseInvoiceRequest request) {
        return post("courses/invoice", request, CrmCourseInvoiceDto.class);
    }

    @Override
    public List<CrmMentorPayrollDto> getMentorPayroll() {
        return getList("mentor/payroll", new GenericType<>() {});
    }

    @Override
    public CrmMentorPayrollDto createMentorPayroll(CrmMentorPayrollRequest request) {
        return post("mentor/payroll", request, CrmMentorPayrollDto.class);
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

        throw new BusinessException("CRM call failed: " + method + " " + baseUrl + "/" + path + " -> " + status + "; body=" + body);
    }
}
