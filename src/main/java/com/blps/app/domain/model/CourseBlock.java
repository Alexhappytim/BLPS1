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
@Table(name = "course_block")
public class CourseBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private long openCost;

    @Column(nullable = false)
    private int orderIndex;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    protected CourseBlock() {
    }

    public CourseBlock(String code, String title, long openCost, int orderIndex, Course course) {
        this.code = code;
        this.title = title;
        this.openCost = openCost;
        this.orderIndex = orderIndex;
        this.course = course;
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

    public long getOpenCost() {
        return openCost;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public Course getCourse() {
        return course;
    }

    public void update(String code, String title, long openCost, int orderIndex, Course course) {
        this.code = code;
        this.title = title;
        this.openCost = openCost;
        this.orderIndex = orderIndex;
        this.course = course;
    }
}
