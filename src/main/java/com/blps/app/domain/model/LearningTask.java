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

    @Column(nullable = false)
    private boolean requiresMentorReview;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "block_id")
    private CourseBlock block;

    protected LearningTask() {
    }

    public LearningTask(String code, String title, long basePoints, boolean requiresMentorReview, CourseBlock block) {
        this.code = code;
        this.title = title;
        this.basePoints = basePoints;
        this.requiresMentorReview = requiresMentorReview;
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

    public boolean isRequiresMentorReview() {
        return requiresMentorReview;
    }

    public CourseBlock getBlock() {
        return block;
    }
}
