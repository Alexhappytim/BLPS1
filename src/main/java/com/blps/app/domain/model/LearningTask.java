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

@Entity
@Table(name = "learning_task")
public class LearningTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private long basePoints;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewType reviewType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "block_id")
    private CourseBlock block;

    protected LearningTask() {
    }

    public LearningTask(String code, String title, long basePoints, ReviewType reviewType, CourseBlock block) {
        this.code = code;
        this.title = title;
        this.basePoints = basePoints;
        this.reviewType = reviewType;
        this.block = block;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public long getBasePoints() {
        return basePoints;
    }

    public ReviewType getReviewType() {
        return reviewType;
    }

    public boolean isRequiresMentorReview() {
        return reviewType == ReviewType.MENTOR;
    }

    public CourseBlock getBlock() {
        return block;
    }

    public void update(String code, String title, long basePoints, ReviewType reviewType, CourseBlock block) {
        this.code = code;
        this.title = title;
        this.basePoints = basePoints;
        this.reviewType = reviewType;
        this.block = block;
    }
}
