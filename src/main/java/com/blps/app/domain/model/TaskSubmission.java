package com.blps.app.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "task_submission")
public class TaskSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id")
    private LearningTask task;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status;

    @Column(nullable = false)
    private int attempt;

    @Column(nullable = false)
    private long calculatedPoints;

    private Long awardedPoints;

    @Column(nullable = false)
    private OffsetDateTime submittedAt;

    private OffsetDateTime reviewedAt;

    protected TaskSubmission() {
    }

    public TaskSubmission(AppUser user,
                          LearningTask task,
                          Difficulty difficulty,
                          SubmissionStatus status,
                          int attempt,
                          long calculatedPoints,
                          Long awardedPoints,
                          OffsetDateTime submittedAt) {
        this.user = user;
        this.task = task;
        this.difficulty = difficulty;
        this.status = status;
        this.attempt = attempt;
        this.calculatedPoints = calculatedPoints;
        this.awardedPoints = awardedPoints;
        this.submittedAt = submittedAt;
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public LearningTask getTask() {
        return task;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public int getAttempt() {
        return attempt;
    }

    public long getCalculatedPoints() {
        return calculatedPoints;
    }

    public Long getAwardedPoints() {
        return awardedPoints;
    }

    public OffsetDateTime getSubmittedAt() {
        return submittedAt;
    }

    public OffsetDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void approve(long awardedPoints) {
        this.status = SubmissionStatus.APPROVED;
        this.awardedPoints = awardedPoints;
        this.reviewedAt = OffsetDateTime.now();
    }

    public void reject() {
        this.status = SubmissionStatus.REJECTED;
        this.awardedPoints = 0L;
        this.reviewedAt = OffsetDateTime.now();
    }
}
