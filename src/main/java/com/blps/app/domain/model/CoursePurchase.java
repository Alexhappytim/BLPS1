package com.blps.app.domain.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "course_purchase", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "course_id"})
})
public class CoursePurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    /** The invoice ID returned by the CRM — used to match payment callbacks. */
    @Column(name = "crm_invoice_id", unique = true)
    private String crmInvoiceId;

    /** Price at the time of purchase (snapshot). */
    @Column(nullable = false)
    private long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CoursePurchaseStatus status;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    private OffsetDateTime paidAt;

    protected CoursePurchase() {}

    public CoursePurchase(AppUser user, Course course, String crmInvoiceId, long amount) {
        this.user = user;
        this.course = course;
        this.crmInvoiceId = crmInvoiceId;
        this.amount = amount;
        this.status = CoursePurchaseStatus.PENDING_PAYMENT;
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public AppUser getUser() { return user; }
    public Course getCourse() { return course; }
    public String getCrmInvoiceId() { return crmInvoiceId; }
    public long getAmount() { return amount; }
    public CoursePurchaseStatus getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getPaidAt() { return paidAt; }

    public boolean isPaid() {
        return status == CoursePurchaseStatus.PAID;
    }

    public void markPaid() {
        this.status = CoursePurchaseStatus.PAID;
        this.paidAt = OffsetDateTime.now();
    }

    public void markFailed() {
        this.status = CoursePurchaseStatus.FAILED;
    }
}
