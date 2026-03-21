package com.blps.app.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_course_progress")
public class UserCourseProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(nullable = false)
    private long points;

    private OffsetDateTime certificateSentAt;

    protected UserCourseProgress() {
    }

    public UserCourseProgress(AppUser user, Course course) {
        this.user = user;
        this.course = course;
        this.points = 0;
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public Course getCourse() {
        return course;
    }

    public long getPoints() {
        return points;
    }

    public void addPoints(long delta) {
        this.points += delta;
    }

    public void subtractPoints(long delta) {
        this.points -= delta;
    }

    public boolean isCertificateSent() {
        return certificateSentAt != null;
    }

    public void markCertificateSent() {
        this.certificateSentAt = OffsetDateTime.now();
    }
}
