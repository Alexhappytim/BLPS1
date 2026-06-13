package com.blps.crmmock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/BLPS/hs/skillbox")
public class CrmMockController {

    private static final Logger log = LoggerFactory.getLogger(CrmMockController.class);

    private final List<Object> users = new ArrayList<>();
    private final List<Invoice> invoices = new ArrayList<>();
    private final List<Object> payrolls = new ArrayList<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final WebClient webClient = WebClient.create();

    @Value("${backend.url:http://localhost:8080}")
    private String backendUrl;

    @Value("${mock.callback.delay-ms:2000}")
    private long callbackDelayMs;

    // --- Users ---

    @PostMapping("/users")
    public Object upsertUser(@RequestBody Object request) {
        log.info("Received user upsert: {}", request);
        users.add(request);
        return request;
    }

    @GetMapping("/users")
    public List<Object> getUsers() {
        return users;
    }

    // --- Invoices (Course purchases) ---

    @PostMapping("/courses/invoice")
    public Invoice createInvoice(@RequestBody InvoiceRequest req) {
        log.info("Received invoice request: {}", req);
        
        Invoice invoice = new Invoice(
                UUID.randomUUID().toString(),
                req.userLogin(),
                req.courseCode(),
                req.amount(),
                "PENDING",
                "http://mock-payment-gateway.local/pay",
                OffsetDateTime.now()
        );
        invoices.add(invoice);

        // Schedule async callback to backend
        scheduler.schedule(() -> sendPaymentCallback(invoice.getInvoiceId()), callbackDelayMs, TimeUnit.MILLISECONDS);

        return invoice;
    }

    @GetMapping("/courses/invoice")
    public List<Invoice> getInvoices() {
        return invoices;
    }

    private void sendPaymentCallback(String invoiceId) {
        log.info("Sending payment callback for invoice {} to {}", invoiceId, backendUrl);
        try {
            webClient.post()
                    .uri(backendUrl + "/api/crm/payment-callback")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new CallbackPayload(invoiceId, true))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("Successfully sent callback for invoice {}", invoiceId);
            
            // Update local state
            invoices.stream()
                    .filter(i -> i.getInvoiceId().equals(invoiceId))
                    .findFirst()
                    .ifPresent(i -> i.setStatus("PAID"));
                    
        } catch (Exception e) {
            log.error("Failed to send callback for invoice {}", invoiceId, e);
        }
    }

    // --- Mentor Payroll ---

    @PostMapping("/mentor/payroll")
    public Object createPayroll(@RequestBody Object request) {
        log.info("Received mentor payroll: {}", request);
        payrolls.add(request);
        return request;
    }

    @GetMapping("/mentor/payroll")
    public List<Object> getPayrolls() {
        return payrolls;
    }

    // --- DTOs ---

    public record InvoiceRequest(String userLogin, String courseCode, long amount) {}
    
    public static class Invoice {
        private String invoiceId;
        private String userLogin;
        private String courseCode;
        private long amount;
        private String status;
        private String paymentUrl;
        private OffsetDateTime createdAt;

        public Invoice(String invoiceId, String userLogin, String courseCode, long amount, String status, String paymentUrl, OffsetDateTime createdAt) {
            this.invoiceId = invoiceId;
            this.userLogin = userLogin;
            this.courseCode = courseCode;
            this.amount = amount;
            this.status = status;
            this.paymentUrl = paymentUrl;
            this.createdAt = createdAt;
        }

        public String getInvoiceId() { return invoiceId; }
        public String getUserLogin() { return userLogin; }
        public String getCourseCode() { return courseCode; }
        public long getAmount() { return amount; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getPaymentUrl() { return paymentUrl; }
        public OffsetDateTime getCreatedAt() { return createdAt; }
    }

    public record CallbackPayload(String invoiceId, boolean success) {}
}
